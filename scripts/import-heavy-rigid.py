#!/usr/bin/env python3
"""Extract and publish state-specific driver knowledge-test question banks.

The PDF is the NSW Government's official rigid knowledge-test question bank.
This importer is deliberately idempotent:

  * ``--dry-run`` extracts the PDF, renders question images, reads the live
    question sets, and writes a reconciliation manifest without mutating
    Firebase.
  * ``--stage`` upserts the selected set and questions as inactive documents.
  * ``--activate`` performs the same upsert with the selected set, license type,
    categories, and questions active, and increments ``app_config/data_version``.

Writes use the OAuth token held by the authenticated Firebase CLI session. No
service-account key is stored in the repository. Images are staged into the
matching ``public/questions/<state>/*`` directory for Firebase Hosting.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
import time
import unicodedata
from collections import defaultdict
from dataclasses import dataclass, asdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable
from urllib.error import HTTPError
from urllib.request import Request, urlopen

import pdfplumber
from PIL import Image


REPO_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_PROJECT = "honq-ac8e4"
DATABASE = "(default)"
LICENSE_TYPE_ID = "heavy_rigid"
ASSESSMENT_TYPE_ID = "knowledge_test"
OVERRIDE_PDFTOPPM = Path("/Users/merkost/.cache/codex-runtimes/codex-primary-runtime/dependencies/bin/override/pdftoppm")

BANKS = {
    "rigid": {
        "pdf": Path("/Users/merkost/Downloads/driver-knowledge-test-questions-heavy-rigid.pdf"),
        "state_id": "nsw",
        "question_set_id": "nsw_hr",
        "license_type_id": "heavy_rigid",
        "source_url": "https://www.nsw.gov.au/sites/default/files/2021-09/driver-knowledge-test-questions-heavy-rigid.pdf",
        "source_sha256": "3de9398991c973def924bf4ab0a208389d84bf11755ed6192d2a396b39e3cafa",
        "hosting_relative_dir": "questions/nsw/rigid",
        "hosting_dir": REPO_ROOT / "public" / "questions" / "nsw" / "rigid",
        "build_dir": REPO_ROOT / "build" / "rigid",
        "expected_count": 343,
        "display_name": "Rigid (LR, MR, HR)",
        "short_name": "LR/MR/HR",
        "tags": ["heavy_rigid", "rigid", "lr", "mr", "hr"],
        "base_tags": ["car", "heavy_rigid", "rigid", "lr", "mr", "hr"],
        "source_sets": ["nsw_car"],
        "allowed_option_counts": [3],
    },
    "combination": {
        "pdf": Path("/Users/merkost/Downloads/driver-knowledge-test-questions-heavy-combination.pdf"),
        "state_id": "nsw",
        "question_set_id": "nsw_hc",
        "license_type_id": "heavy_combination",
        "source_url": "https://www.nsw.gov.au/sites/default/files/2021-09/driver-knowledge-test-questions-heavy-combination.pdf",
        "source_sha256": "7431162173ed7bfafff0813fc1d1b8544d59aa488edb73561b127457736e7766",
        "hosting_relative_dir": "questions/nsw/combination",
        "hosting_dir": REPO_ROOT / "public" / "questions" / "nsw" / "combination",
        "build_dir": REPO_ROOT / "build" / "combination",
        "expected_count": 348,
        "display_name": "Heavy Combination (HC & MC)",
        "short_name": "HC/MC",
        "tags": ["heavy_combination", "combination", "hc", "mc"],
        "base_tags": ["heavy_combination", "combination", "hc", "mc"],
        "source_sets": ["nsw_hr", "nsw_car"],
        "allowed_option_counts": [3],
    },
    "rider_smv": {
        "pdf": Path("/Users/merkost/Downloads/driver-knowledge-test-questions-rider-smv.pdf"),
        "state_id": "nsw",
        "question_set_id": "nsw_rsmv",
        "license_type_id": "rider_special_mobility_vehicle",
        "source_url": "https://www.nsw.gov.au/sites/default/files/2021-09/driver-knowledge-test-questions-rider-smv.pdf",
        "source_sha256": "ef6d100373150419482e9ce27c19fc2382ddf5be9d57d0b2572bdff9bf72877d",
        "hosting_relative_dir": "questions/nsw/rsmv",
        "hosting_dir": REPO_ROOT / "public" / "questions" / "nsw" / "rsmv",
        "build_dir": REPO_ROOT / "build" / "rider_smv",
        "expected_count": 275,
        "display_name": "Rider Special Mobility Vehicle",
        "short_name": "RSMV",
        "tags": ["rider_special_mobility_vehicle", "rider_smv", "rsmv", "smv"],
        "base_tags": ["rider_special_mobility_vehicle", "rider_smv", "rsmv", "smv"],
        "source_sets": ["nsw_rider_rkt", "nsw_car"],
        "allowed_option_counts": [2, 3],
    },
    "nt_car": {
        "pdf": Path("/private/tmp/honq-nt-banks/class-c-knowledge-test.pdf"),
        "state_id": "nt",
        "question_set_id": "nt_car",
        "license_type_id": "car",
        "source_url": "https://nt.gov.au/_media/docs/driving,-transport-and-marine/driver_licenses/class-c-knowledge-test.pdf",
        "source_sha256": "6e558873ce1aa53d9c9fb33aa39d21f38d0b3362eec43318f3fdf719d56deb73",
        "hosting_relative_dir": "questions/nt/car",
        "hosting_dir": REPO_ROOT / "public" / "questions" / "nt" / "car",
        "build_dir": REPO_ROOT / "build" / "nt_car",
        "expected_count": 358,
        "display_name": "Car",
        "short_name": "Car",
        "tags": ["car"],
        "base_tags": ["car"],
        "source_sets": ["nt_car"],
        "allowed_option_counts": [3],
    },
    "nt_rider": {
        "pdf": Path("/private/tmp/honq-nt-banks/class-r-knowledge-test.pdf"),
        "state_id": "nt",
        "question_set_id": "nt_rider",
        "license_type_id": "rider",
        "source_url": "https://nt.gov.au/_media/docs/driving,-transport-and-marine/driver_licenses/class-r-knowledge-test.pdf",
        "source_sha256": "c3103dee52530c33676f10059e9c99d8b7e4496803d71f009dbb270e71cc6390",
        "hosting_relative_dir": "questions/nt/rider",
        "hosting_dir": REPO_ROOT / "public" / "questions" / "nt" / "rider",
        "build_dir": REPO_ROOT / "build" / "nt_rider",
        "expected_count": 350,
        "display_name": "Rider",
        "short_name": "Rider",
        "tags": ["rider"],
        "base_tags": ["rider", "car"],
        "source_sets": ["nt_rider", "nt_car"],
        "allowed_option_counts": [3],
    },
    "nt_rigid": {
        "pdf": Path("/private/tmp/honq-nt-banks/heavy-vehicle-knowledge-test-rigid.pdf"),
        "state_id": "nt",
        "question_set_id": "nt_hr",
        "license_type_id": "heavy_rigid",
        "source_url": "https://nt.gov.au/_media/docs/driving,-transport-and-marine/heavy_vehicles/get-your-heavy-vehicle-licence/heavy-vehicle-knowledge-test-rigid.pdf",
        "source_sha256": "6d84572ae3f6181da2bc72f7afd00f8f31415bd5bf6fa50df2a07afb1e0ee22b",
        "hosting_relative_dir": "questions/nt/rigid",
        "hosting_dir": REPO_ROOT / "public" / "questions" / "nt" / "rigid",
        "build_dir": REPO_ROOT / "build" / "nt_rigid",
        "expected_count": 289,
        "display_name": "Rigid (LR, MR, HR)",
        "short_name": "LR/MR/HR",
        "tags": ["heavy_rigid", "rigid", "lr", "mr", "hr"],
        "base_tags": ["car", "heavy_rigid", "rigid", "lr", "mr", "hr"],
        "source_sets": ["nt_car"],
        "allowed_option_counts": [3],
    },
    "nt_articulated": {
        "pdf": Path("/private/tmp/honq-nt-banks/heavy-vehicle-knowledge-test-articulated.pdf"),
        "state_id": "nt",
        "question_set_id": "nt_hc",
        "license_type_id": "heavy_combination",
        "source_url": "https://nt.gov.au/_media/docs/driving,-transport-and-marine/heavy_vehicles/get-your-heavy-vehicle-licence/heavy-vehicle-knowledge-test-articulated.pdf",
        "source_sha256": "1963ec156bf3d0579ea545850fceb3daa072412ce4c9c0e329bc83638ff7479b",
        "hosting_relative_dir": "questions/nt/articulated",
        "hosting_dir": REPO_ROOT / "public" / "questions" / "nt" / "articulated",
        "build_dir": REPO_ROOT / "build" / "nt_articulated",
        "expected_count": 298,
        "display_name": "Heavy Combination (HC & MC)",
        "short_name": "HC/MC",
        "tags": ["heavy_combination", "combination", "hc", "mc"],
        "base_tags": ["heavy_combination", "combination", "hc", "mc"],
        "source_sets": ["nt_rigid", "nt_car"],
        "allowed_option_counts": [3],
    },
}

PREFIX_TO_CATEGORY = {
    "ICAC": "icac",
    "GK": "general_knowledge",
    "CG": "general_knowledge",
    "LG": "heavy_vehicle_general",
    "RG": "heavy_vehicle_general",
    "AD": "alcohol_and_drugs",
    "DR": "alcohol_and_drugs",
    "BI": "bicycle_safety",
    "FD": "fatigue_and_defensive_driving",
    "IN": "intersections",
    "LD": "traffic_lights_lanes",
    "TL": "traffic_lights_lanes",
    "LR": "loading_and_restraints",
    "ND": "negligent_driving",
    "PD": "pedestrians",
    "RS": "rider_safety",
    "SB": "seat_belts_restraints",
    "SL": "speed_limits",
    "SI": "traffic_signs",
    "CGK": "general_knowledge",
    "CAD": "alcohol_and_drugs",
    "CFD": "fatigue_and_defensive_driving",
    "CIN": "intersections",
    "CLD": "traffic_lights_lanes",
    "CND": "negligent_driving",
    "CSB": "seat_belts_restraints",
    "CSL": "speed_limits",
    "MGK": "rider_safety",
    "RAD": "alcohol_and_drugs",
    "RFD": "fatigue_and_defensive_driving",
    "SRB": "rider_safety",
    "SRH": "rider_safety",
    "SRM": "rider_safety",
    "HGK": "heavy_vehicle_general",
    "HRGK": "heavy_vehicle_general",
    "HFD": "fatigue_and_defensive_driving",
    "HLR": "loading_and_restraints",
    "HCGK": "heavy_vehicle_general",
}
QUESTION_CODE = re.compile(r"^[A-Z]{1,8}\d+$")
OPTION_NUMERIC = re.compile(r"^\d+(?:\.\d+)?$")

STATE_ID = "nsw"
QUESTION_SET_ID = "nsw_hr"
SOURCE_URL = ""
HOSTING_RELATIVE_DIR = ""
HOSTING_DIR = REPO_ROOT / "public"
BUILD_DIR = REPO_ROOT / "build"
RIGID_TAGS: list[str] = []
CAR_RIGID_TAGS: list[str] = []
EXPECTED_COUNT = 0
ALLOWED_OPTION_COUNTS: set[int] = {3}


def configure_bank(bank_name: str) -> dict[str, Any]:
    global QUESTION_SET_ID, LICENSE_TYPE_ID, SOURCE_URL, STATE_ID
    global HOSTING_RELATIVE_DIR, HOSTING_DIR, BUILD_DIR, RIGID_TAGS, CAR_RIGID_TAGS, EXPECTED_COUNT, ALLOWED_OPTION_COUNTS
    try:
        config = BANKS[bank_name]
    except KeyError as error:
        raise ValueError(f"Unknown bank: {bank_name}") from error
    STATE_ID = config["state_id"]
    QUESTION_SET_ID = config["question_set_id"]
    LICENSE_TYPE_ID = config["license_type_id"]
    SOURCE_URL = config["source_url"]
    HOSTING_RELATIVE_DIR = config["hosting_relative_dir"]
    HOSTING_DIR = config["hosting_dir"]
    BUILD_DIR = config["build_dir"]
    EXPECTED_COUNT = config["expected_count"]
    RIGID_TAGS = list(config["tags"])
    CAR_RIGID_TAGS = list(config["base_tags"])
    ALLOWED_OPTION_COUNTS = set(config["allowed_option_counts"])
    return config


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="milliseconds").replace("+00:00", "Z")


def norm(value: str) -> str:
    value = unicodedata.normalize("NFKC", value or "")
    value = value.replace("’", "'").replace("–", "-").replace("—", "-")
    return re.sub(r"[^a-z0-9]+", "", value.lower())


def text_norm(value: str) -> str:
    value = unicodedata.normalize("NFKC", value or "")
    value = value.replace("’", "'").replace("–", "-").replace("—", "-")
    value = re.sub(
        r"\b(?:Class\s+[A-Z]+\s+Knowledge\s+Test\s+)?Motor\s+Vehicle\s+Registry\s+"
        r"[A-Za-z]+\s+\d{4}\s+Page\s+\d+\s+of\s+\d+\b",
        " ",
        value,
        flags=re.IGNORECASE,
    )
    value = re.sub(r"\s+", " ", value).strip()
    return value


def comparable(value: str) -> str:
    """Normalize wording while removing PDF/export artefacts such as RUH8."""
    value = re.sub(r"\b(?:RUH|HVH|HVDH|LRG)\d*\b", " ", value or "", flags=re.IGNORECASE)
    value = re.sub(r"\b(?:GENERAL KNOWLEDGE|ROAD SAFETY|TRAFFIC SIGNS) SECTION\b", " ", value, flags=re.IGNORECASE)
    # The January 2021 NT PDFs put a repeated document footer at the end of
    # whichever option happens to cross the page boundary.  Older imports
    # retain that footer, while the current parser strips it from source text;
    # ignore it for overlap reconciliation so a harmless layout artefact is
    # not reported as an answer conflict.
    value = re.sub(
        r"\b(?:Class\s+[A-Z]+\s+Knowledge\s+Test\s+)?Motor\s+Vehicle\s+Registry\s+"
        r"[A-Za-z]+\s+\d{4}\s+Page\s+\d+\s+of\s+\d+\b",
        " ",
        value,
        flags=re.IGNORECASE,
    )
    return norm(value)


def firestore_value(value: Any) -> dict[str, Any]:
    if value is None:
        return {"nullValue": None}
    if isinstance(value, bool):
        return {"booleanValue": value}
    if isinstance(value, int):
        return {"integerValue": str(value)}
    if isinstance(value, float):
        return {"doubleValue": value}
    if isinstance(value, list):
        return {"arrayValue": {"values": [firestore_value(item) for item in value]}}
    if isinstance(value, dict):
        return {"mapValue": {"fields": {key: firestore_value(item) for key, item in value.items()}}}
    return {"stringValue": str(value)}


def plain_value(value: dict[str, Any]) -> Any:
    if "stringValue" in value:
        return value["stringValue"]
    if "integerValue" in value:
        return int(value["integerValue"])
    if "doubleValue" in value:
        return value["doubleValue"]
    if "booleanValue" in value:
        return value["booleanValue"]
    if "nullValue" in value:
        return None
    if "timestampValue" in value:
        return value["timestampValue"]
    if "arrayValue" in value:
        return [plain_value(item) for item in value["arrayValue"].get("values", [])]
    if "mapValue" in value:
        return {key: plain_value(item) for key, item in value["mapValue"].get("fields", {}).items()}
    return value


def doc_plain(document: dict[str, Any]) -> dict[str, Any]:
    return {key: plain_value(value) for key, value in document.get("fields", {}).items()}


class FirestoreRest:
    def __init__(self, project: str):
        self.project = project
        self.base = f"https://firestore.googleapis.com/v1/projects/{project}/databases/{DATABASE}/documents"
        self.token = self._load_cli_token()

    @staticmethod
    def _load_cli_token() -> str:
        candidates = [
            Path.home() / ".config" / "configstore" / "firebase-tools.json",
            Path.home() / ".config" / "configstore" / "firebase-tools.json5",
        ]
        for path in candidates:
            if not path.exists():
                continue
            try:
                data = json.loads(path.read_text())
                token = data.get("tokens", {}).get("access_token")
                if token:
                    return token
            except (OSError, json.JSONDecodeError):
                continue
        raise RuntimeError("No Firebase CLI OAuth token found. Run `firebase login` first.")

    def request(self, method: str, url: str, payload: dict[str, Any] | None = None) -> Any:
        body = None if payload is None else json.dumps(payload).encode("utf-8")
        request = Request(
            url,
            data=body,
            method=method,
            headers={
                "Authorization": f"Bearer {self.token}",
                "Content-Type": "application/json",
            },
        )
        for attempt in range(5):
            try:
                with urlopen(request, timeout=60) as response:
                    content = response.read()
                    return json.loads(content) if content else {}
            except HTTPError as error:
                message = error.read().decode("utf-8", errors="replace")
                if error.code not in {429, 500, 502, 503, 504} or attempt == 4:
                    raise RuntimeError(f"Firestore {method} {url} failed ({error.code}): {message}") from error
                delay = min(16, 2 ** attempt)
                print(f"[firestore] HTTP {error.code}; retrying in {delay}s", file=sys.stderr)
                time.sleep(delay)

    def list_collection(self, collection: str) -> list[dict[str, Any]]:
        documents: list[dict[str, Any]] = []
        token: str | None = None
        while True:
            params = "?pageSize=300"
            if token:
                params += f"&pageToken={token}"
            result = self.request("GET", f"{self.base}/{collection}{params}")
            documents.extend(result.get("documents", []))
            token = result.get("nextPageToken")
            if not token:
                return documents

    def query_collection(self, collection: str, field: str, values: list[str]) -> list[dict[str, Any]]:
        if not values:
            return []
        structured_query = {
            "structuredQuery": {
                "from": [{"collectionId": collection}],
                "where": {
                    "fieldFilter": {
                        "field": {"fieldPath": field},
                        "op": "IN",
                        "value": {"arrayValue": {"values": [firestore_value(value) for value in values]}},
                    }
                },
            }
        }
        result = self.request("POST", f"{self.base}:runQuery", structured_query)
        return [item["document"] for item in result if item.get("document")]

    def get(self, path: str) -> dict[str, Any] | None:
        try:
            return self.request("GET", f"{self.base}/{path}")
        except RuntimeError as error:
            if "(404)" in str(error):
                return None
            raise

    def commit(self, writes: list[dict[str, Any]]) -> None:
        for start in range(0, len(writes), 450):
            self.request("POST", f"{self.base}:commit", {"writes": writes[start:start + 450]})


@dataclass
class RigidQuestion:
    code: str
    text: str
    options: list[str]
    correct_index: int
    category: str
    page: int
    has_image: bool
    image_url: str | None = None


def group_lines(words: list[dict[str, Any]]) -> list[list[dict[str, Any]]]:
    lines: list[tuple[float, list[dict[str, Any]]]] = []
    for word in sorted(words, key=lambda item: (item["top"], item["x0"])):
        match = next((line for line in lines if abs(line[0] - word["top"]) <= 1.25), None)
        if match is None:
            lines.append((word["top"], [word]))
        else:
            match[1].append(word)
    return [sorted(line[1], key=lambda item: item["x0"]) for line in sorted(lines, key=lambda item: item[0])]


def line_text(line: list[dict[str, Any]]) -> str:
    return " ".join(word["text"] for word in line).strip()


def question_code(value: str) -> str | None:
    candidate = value.rstrip("-–—")
    return candidate if QUESTION_CODE.fullmatch(candidate) else None


def header_code(line: list[dict[str, Any]]) -> tuple[str, int] | None:
    """Return a question code and its word index for NSW or NT header layouts."""
    words = sorted(line, key=lambda item: item["x0"])
    for index, word in enumerate(words[:3]):
        code = question_code(word["text"])
        if not code:
            continue
        prefix = re.match(r"[A-Z]+", code)
        if not prefix or prefix.group(0) not in PREFIX_TO_CATEGORY:
            continue
        if index == 0 or (index == 1 and re.fullmatch(r"\d+\.", words[0]["text"])):
            return code, index
    return None


def is_option_start(line: list[dict[str, Any]]) -> bool:
    if not line:
        return False
    first = line[0]["text"]
    if first in {"o", ""} and len(line) >= 2 and re.fullmatch(r"\([a-z]\)", line[1]["text"]):
        return True
    if first in {"-", "−", "–"} and len(line) == 1:
        return False
    if first in {"-", "−", "–", "-All"} or first.startswith(("-", "−", "–")):
        return True
    if first == "Diagram":
        return True
    # A handful of diagram questions omit the dash and indent their three
    # answer labels (for example "Vehicles 1 and 3.") at the option column.
    if 120 <= line[0]["x0"] <= 140 and line[0]["fontname"] in {"ArialMT", "Arial-Black", "Arial-BoldMT"}:
        # Diagram labels such as "1 2" and "80% 50%" are not answer starts.
        if not all(re.fullmatch(r"(?:\d+(?:\.\d+)?%?|[‐-])", word["text"]) for word in line):
            return True
    return (
        OPTION_NUMERIC.fullmatch(first) is not None
        and 120 <= line[0]["x0"] <= 140
        and any(not OPTION_NUMERIC.fullmatch(word["text"]) for word in line[1:])
    )


def is_diagram_label_line(line: list[dict[str, Any]]) -> bool:
    """Drop numeric labels painted inside PDF diagrams from question text."""
    if not line:
        return False
    return all(re.fullmatch(r"\d+(?:\.\d+)?%?", word["text"]) for word in line)


def parse_pdf(pdf_path: Path) -> list[RigidQuestion]:
    records: list[RigidQuestion] = []
    with pdfplumber.open(str(pdf_path)) as pdf:
        for page_number, page in enumerate(pdf.pages, start=1):
            words = page.extract_words(extra_attrs=["fontname", "size"], keep_blank_chars=False)
            lines = group_lines(words)
            headers: list[tuple[int, str, int]] = []
            for index, line in enumerate(lines):
                parsed_header = header_code(line)
                if parsed_header:
                    code, code_word_index = parsed_header
                    headers.append((index, code, code_word_index))
            for header_index, (line_index, code, code_word_index) in enumerate(headers):
                end_index = headers[header_index + 1][0] if header_index + 1 < len(headers) else len(lines)
                block = lines[line_index + 1:end_index]
                block = [
                    line for line in block
                    if line and not (
                        len(line) == 1
                        and line[0]["x0"] > 430
                        and (line[0]["text"].isdigit() or re.fullmatch(r"[A-Z]{2,5}\d*", line[0]["text"]))
                    ) and not is_diagram_label_line(line)
                    and not (len(line) == 1 and line[0]["text"] in {"-", "−", "–"})
                ]
                option_indices = [i for i, line in enumerate(block) if is_option_start(line)]
                if len(option_indices) not in ALLOWED_OPTION_COUNTS:
                    expected = ", ".join(str(count) for count in sorted(ALLOWED_OPTION_COUNTS))
                    raise ValueError(f"{code} on page {page_number}: expected {expected} options, found {len(option_indices)}")
                header_question = lines[line_index][code_word_index + 1:]
                if header_question and header_question[0]["text"] in {"-", "−", "–", "—"}:
                    header_question = header_question[1:]
                question_lines = ([header_question] if header_question else []) + block[:option_indices[0]]
                options: list[str] = []
                correct_options: list[bool] = []
                for option_number, start in enumerate(option_indices):
                    stop = option_indices[option_number + 1] if option_number + 1 < len(option_indices) else len(block)
                    option_words = [word for line in block[start:stop] for word in line]
                    marker = option_words[0]["text"] if option_words else ""
                    if marker in {"-", "−", "–", "—", "o", ""}:
                        option_words = option_words[1:]
                    if option_words and re.fullmatch(r"\([a-z]\)", option_words[0]["text"]):
                        option_words = option_words[1:]
                    option_text = line_text(option_words)
                    options.append(text_norm(option_text))
                    first_line_words = list(block[start])
                    first_marker = first_line_words[0]["text"] if first_line_words else ""
                    if first_marker in {"-", "−", "–", "—", "o", ""}:
                        first_line_words = first_line_words[1:]
                    if first_line_words and re.fullmatch(r"\([a-z]\)", first_line_words[0]["text"]):
                        first_line_words = first_line_words[1:]
                    is_correct = marker == "" or any(
                        "Black" in word.get("fontname", "") or "Bold" in word.get("fontname", "")
                        for word in first_line_words
                    )
                    correct_options.append(is_correct)
                correct = [i for i, is_correct in enumerate(correct_options) if is_correct]
                if len(correct) != 1:
                    raise ValueError(f"{code} on page {page_number}: expected one correct answer, got {correct}")
                question_text = text_norm(line_text([word for line in question_lines for word in line]))
                question_text = question_text.rstrip("-−– ").rstrip()
                prefix = re.match(r"[A-Z]+", code)
                if not prefix or prefix.group(0) not in PREFIX_TO_CATEGORY:
                    raise ValueError(f"{code}: no category mapping")
                header_top = lines[line_index][0]["top"]
                next_header_top = lines[headers[header_index + 1][0]][0]["top"] if header_index + 1 < len(headers) else page.height
                has_image = any(
                    image["bottom"] > header_top - 2 and image["top"] < next_header_top - 2
                    for image in page.images
                )
                records.append(RigidQuestion(
                    code=code,
                    text=question_text,
                    options=options,
                    correct_index=correct[0],
                    category=PREFIX_TO_CATEGORY[prefix.group(0)],
                    page=page_number,
                    has_image=has_image,
                ))
    if len(records) != len({record.code for record in records}):
        raise ValueError("PDF contains duplicate question codes")
    if EXPECTED_COUNT and (len(records) != EXPECTED_COUNT or any(len(record.options) not in ALLOWED_OPTION_COUNTS for record in records)):
        raise ValueError(f"Unexpected PDF shape: {len(records)} questions")
    return records


def render_images(pdf_path: Path, records: list[RigidQuestion], output_dir: Path) -> int:
    output_dir.mkdir(parents=True, exist_ok=True)
    by_page: dict[int, list[RigidQuestion]] = defaultdict(list)
    for record in records:
        if record.has_image:
            by_page[record.page].append(record)
    rendered = 0
    render_dir = BUILD_DIR / "rendered-pages"
    render_dir.mkdir(parents=True, exist_ok=True)
    with pdfplumber.open(str(pdf_path)) as pdf:
        for page_number, page_records in by_page.items():
            page = pdf.pages[page_number - 1]
            output = render_dir / f"page-{page_number}.png"
            if not output.exists():
                command = [
                    str(OVERRIDE_PDFTOPPM), "-f", str(page_number), "-l", str(page_number),
                    "-r", "160", "-png", "-singlefile", str(pdf_path), str(render_dir / f"page-{page_number}"),
                ]
                subprocess.run(command, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
            raster = Image.open(output)
            scale = raster.width / page.width
            header_lines = []
            words = page.extract_words(extra_attrs=["fontname", "size"], keep_blank_chars=False)
            for line in group_lines(words):
                parsed_header = header_code(line)
                if parsed_header:
                    code, _ = parsed_header
                    header_lines.append((code, line[0]["top"]))
            for record in page_records:
                header_top = next(top for code, top in header_lines if code == record.code)
                later = [top for code, top in header_lines if top > header_top]
                end_top = min(later) - 2 if later else page.height - 25
                images = [
                    image for image in page.images
                    if image["bottom"] > header_top - 2
                    and image["top"] < end_top
                    and (
                        image.get("width", 0) >= 25
                        or image.get("height", 0) >= 25
                        or image.get("srcsize", (0, 0))[0] >= 30
                    )
                ]
                if not images:
                    record.has_image = False
                    stale_path = output_dir / f"{record.code}.png"
                    if stale_path.exists():
                        stale_path.unlink()
                    continue
                left = max(0, min(image["x0"] for image in images) - 3)
                top = max(0, min(image["top"] for image in images) - 3)
                right = min(page.width, max(image["x1"] for image in images) + 3)
                bottom = min(page.height, max(image["bottom"] for image in images) + 3)
                crop = raster.crop((int(left * scale), int(top * scale), int(right * scale), int(bottom * scale)))
                path = output_dir / f"{record.code}.png"
                crop.save(path, format="PNG", optimize=True)
                record.image_url = f"{HOSTING_RELATIVE_DIR}/{record.code}.png"
                rendered += 1
            raster.close()
    return rendered


def stable_shuffle(options: list[str], correct_index: int, code: str) -> tuple[list[str], int]:
    # A tiny deterministic permutation avoids the PDF's answer-order bias while
    # keeping repeated imports byte-for-byte stable.
    order = sorted(range(len(options)), key=lambda index: hashlib.sha256(f"{code}:{index}".encode()).hexdigest())
    shuffled = [options[index] for index in order]
    return shuffled, order.index(correct_index)


def load_properties() -> dict[str, str]:
    properties: dict[str, str] = {}
    path = REPO_ROOT / "local.properties"
    if not path.exists():
        return properties
    for line in path.read_text().splitlines():
        if "=" in line and not line.strip().startswith("#"):
            key, value = line.split("=", 1)
            properties[key.strip()] = value.strip()
    return properties


def document_path(collection: str, document_id: str, project: str) -> str:
    return f"projects/{project}/databases/{DATABASE}/documents/{collection}/{document_id}"


def write_document(collection: str, document_id: str, fields: dict[str, Any], project: str) -> dict[str, Any]:
    return {
        "update": {
            "name": document_path(collection, document_id, project),
            "fields": {key: firestore_value(value) for key, value in fields.items()},
        }
    }


def normalize_existing_question(doc: dict[str, Any]) -> dict[str, Any]:
    return doc_plain(doc)


def equivalent(pdf_question: RigidQuestion, existing: dict[str, Any]) -> bool:
    if comparable(str(existing.get("text", ""))) != comparable(pdf_question.text):
        return False
    existing_options = [text_norm(str(option)) for option in existing.get("options", [])]
    if {comparable(option) for option in existing_options} != {comparable(option) for option in pdf_question.options}:
        return False
    existing_index = int(existing.get("correct_index", -1))
    if not 0 <= existing_index < len(existing_options):
        return False
    return comparable(existing_options[existing_index]) == comparable(pdf_question.options[pdf_question.correct_index])


def make_question_docs(
    records: list[RigidQuestion],
    existing_questions: dict[str, dict[str, Any]],
    active: bool,
    timestamp: str,
) -> tuple[list[dict[str, Any]], dict[str, int]]:
    docs: list[dict[str, Any]] = []
    counts = {"safe_overlap": 0, "conflict": 0, "new": 0, "images": 0}
    for record in records:
        existing = existing_questions.get(record.code)
        safe_overlap = existing is not None and equivalent(record, existing)
        if safe_overlap:
            counts["safe_overlap"] += 1
            fields = dict(existing)
            # Reuse the target qset's existing document ID.  Cross-qset
            # overlaps get a deterministic copy in the new qset.
            same_set = existing.get("question_set_id") == QUESTION_SET_ID
            fields["id"] = existing.get("id") if same_set else f"{QUESTION_SET_ID}__{record.code}"
            fields["question_set_id"] = QUESTION_SET_ID
            fields["state_id"] = STATE_ID
            fields["is_active"] = active
            fields["updated_at"] = timestamp
            fields["text"] = record.text
            fields["options"], fields["correct_index"] = stable_shuffle(record.options, record.correct_index, record.code)
            fields["category"] = record.category
            fields["source"] = SOURCE_URL
            fields["license_tags"] = sorted(set(fields.get("license_tags", []) + CAR_RIGID_TAGS))
            fields["source_question_id"] = existing.get("id", record.code)
            # Reuse the already-published car image for equivalent questions.
            if existing.get("image_url"):
                fields["image_url"] = existing["image_url"]
            elif record.image_url:
                # An equivalent source question may predate image extraction;
                # use the bank-specific crop so no PDF image is dropped.
                fields["image_url"] = record.image_url
        else:
            counts["conflict" if existing is not None else "new"] += 1
            options, correct_index = stable_shuffle(record.options, record.correct_index, record.code)
            same_set = existing is not None and existing.get("question_set_id") == QUESTION_SET_ID
            fields = {
                # A conflict already in the target qset is an older source
                # extraction for the same bank; repair it in place after the
                # explicit source-code reconciliation.  Cross-qset conflicts
                # remain deterministic source-specific copies.
                "id": existing.get("id") if same_set else f"{QUESTION_SET_ID}__{record.code}",
                "code": record.code,
                "text": record.text,
                "image_url": record.image_url,
                "options": options,
                "correct_index": correct_index,
                "explanation": "",
                "category": record.category,
                "question_set_id": QUESTION_SET_ID,
                "updated_at": timestamp,
                "state_id": STATE_ID,
                "difficulty": 2,
                "is_active": active,
                "version": 1,
                "source": SOURCE_URL,
                "created_at": timestamp,
                "license_tags": list(RIGID_TAGS),
                "source_question_id": existing.get("id", record.code) if existing else None,
            }
        if fields.get("image_url"):
            counts["images"] += 1
        docs.append(fields)
    return docs, counts


def build_reconciliation(records: list[RigidQuestion], existing_questions: dict[str, dict[str, Any]], pdf_sha256: str) -> dict[str, Any]:
    safe = [record.code for record in records if record.code in existing_questions and equivalent(record, existing_questions[record.code])]
    conflicts = [record.code for record in records if record.code in existing_questions and not equivalent(record, existing_questions[record.code])]
    new = [record.code for record in records if record.code not in existing_questions]
    return {
        "pdf_sha256": pdf_sha256,
        "source_url": SOURCE_URL,
        "state_id": STATE_ID,
        "license_type_id": LICENSE_TYPE_ID,
        "question_count": len(records),
        "safe_overlap_count": len(safe),
        "conflict_count": len(conflicts),
        "new_count": len(new),
        "safe_overlap_codes": safe,
        "conflict_codes": conflicts,
        "new_codes": new,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bank", choices=sorted(BANKS), default="rigid")
    parser.add_argument("--pdf", type=Path, default=None)
    parser.add_argument("--project", default=None)
    parser.add_argument("--mode", choices=["dry-run", "stage", "activate"], default="dry-run")
    parser.add_argument("--skip-images", action="store_true")
    parser.add_argument("--offline", action="store_true", help="Parse and render without reading Firebase (dry-run only)")
    args_local = parser.parse_args()
    bank = configure_bank(args_local.bank)
    pdf_path = args_local.pdf or bank["pdf"]
    if not pdf_path.exists():
        raise SystemExit(f"PDF not found: {pdf_path}")
    properties = load_properties()
    project = args_local.project or properties.get("firebase.project.id") or DEFAULT_PROJECT
    BUILD_DIR.mkdir(parents=True, exist_ok=True)
    records = parse_pdf(pdf_path)
    image_count = 0 if args_local.skip_images else render_images(pdf_path, records, HOSTING_DIR)
    print(f"[extract] {len(records)} questions; {image_count} images staged in {HOSTING_DIR}")

    existing_questions: dict[str, dict[str, Any]] = {}
    existing_document_ids: dict[str, str] = {}
    firestore: FirestoreRest | None = None
    if args_local.offline:
        if args_local.mode != "dry-run":
            raise SystemExit("--offline is only valid with --mode dry-run")
    else:
        firestore = FirestoreRest(project)
        question_documents = firestore.query_collection("questions", "question_set_id", bank["source_sets"])
        for document in question_documents:
            plain = doc_plain(document)
            code = plain.get("code")
            if plain.get("question_set_id") not in bank["source_sets"] or not code:
                continue
            # Firestore does not guarantee runQuery order. Prefer an existing
            # document from the target qset so a refresh updates it in place;
            # only fall back to a shared/base qset for cross-bank overlap.
            current = existing_questions.get(code)
            if current is None or (
                plain.get("question_set_id") == QUESTION_SET_ID
                and current.get("question_set_id") != QUESTION_SET_ID
            ):
                existing_questions[code] = plain
                existing_document_ids[code] = document["name"].rsplit("/", 1)[-1]
    timestamp = now_iso()
    question_docs, counts = make_question_docs(records, existing_questions, active=args_local.mode == "activate", timestamp=timestamp)
    pdf_sha256 = hashlib.sha256(pdf_path.read_bytes()).hexdigest()
    expected_sha256 = bank.get("source_sha256")
    if expected_sha256 and pdf_sha256 != expected_sha256:
        raise SystemExit(f"Source SHA-256 mismatch for {args_local.bank}: expected {expected_sha256}, got {pdf_sha256}")
    reconciliation = build_reconciliation(records, existing_questions, pdf_sha256)
    reconciliation.update({"counts": counts, "generated_at": timestamp, "project": project, "question_set_id": QUESTION_SET_ID})
    (BUILD_DIR / "reconciliation.json").write_text(json.dumps(reconciliation, indent=2, sort_keys=True) + "\n")
    (BUILD_DIR / "question-set.json").write_text(json.dumps(question_docs, indent=2, sort_keys=True) + "\n")
    print("[reconcile]", json.dumps({key: reconciliation[key] for key in ("question_count", "safe_overlap_count", "conflict_count", "new_count")}, sort_keys=True))
    if args_local.mode == "dry-run":
        return

    if firestore is None:
        raise RuntimeError("Firebase client was not initialized")

    active = args_local.mode == "activate"
    existing_license = firestore.get(f"license_types/{LICENSE_TYPE_ID}")
    existing_set = firestore.get(f"question_sets/{QUESTION_SET_ID}")
    license_fields = doc_plain(existing_license) if existing_license else {
        "id": LICENSE_TYPE_ID,
        "created_at": timestamp,
        "display_order": 4,
    }
    license_fields.update({
        "name": bank["display_name"],
        "short_name": bank["short_name"],
        "is_active": active,
        "updated_at": timestamp,
    })
    set_fields = doc_plain(existing_set) if existing_set else {
        "id": QUESTION_SET_ID,
        "state_id": STATE_ID,
        "license_type_id": LICENSE_TYPE_ID,
        "assessment_type_id": ASSESSMENT_TYPE_ID,
        "mock_test_question_count": 45,
        "mock_test_time_limit_minutes": 45,
        "mock_test_pass_percentage": 75,
        "created_at": timestamp,
    }
    set_fields.update({"state_id": STATE_ID, "license_type_id": LICENSE_TYPE_ID, "is_active": active, "updated_at": timestamp})

    category_ids = sorted({record.category for record in records})
    category_documents = {document["name"].rsplit("/", 1)[-1]: doc_plain(document) for document in firestore.list_collection("categories")}
    junction_documents = {document["name"].rsplit("/", 1)[-1]: doc_plain(document) for document in firestore.list_collection("question_set_categories")}
    writes = [
        write_document("license_types", LICENSE_TYPE_ID, license_fields, project),
        write_document("question_sets", QUESTION_SET_ID, set_fields, project),
    ]
    for category_id in category_ids:
        category = category_documents.get(category_id)
        if not category:
            category = {
                "id": category_id,
                "name": category_id.replace("_", " ").title(),
                "description": "",
                "icon_name": "",
                "display_order": 0,
                "created_at": timestamp,
            }
        category["is_active"] = active if active else category.get("is_active", False)
        category["updated_at"] = timestamp
        writes.append(write_document("categories", category_id, category, project))
        key = f"{QUESTION_SET_ID}__{category_id}"
        junction = junction_documents.get(key, {
            "id": key,
            "question_set_id": QUESTION_SET_ID,
            "category_id": category_id,
            "display_order": category.get("display_order", 0),
        })
        junction["is_active"] = active if active else junction.get("is_active", False)
        writes.append(write_document("question_set_categories", key, junction, project))
    # Tag equivalent, already-published questions in place. Conflicting answer
    # keys remain in their original set; the new copy uses the official PDF.
    for code in reconciliation["safe_overlap_codes"]:
        existing = dict(existing_questions[code])
        existing["license_tags"] = sorted(set(existing.get("license_tags", []) + RIGID_TAGS))
        existing["updated_at"] = timestamp
        writes.append(write_document("questions", existing_document_ids[code], existing, project))
    writes.extend(write_document("questions", fields["id"], fields, project) for fields in question_docs)
    if active:
        config = firestore.get("app_config/data_version")
        config_fields = doc_plain(config) if config else {"key": "data_version"}
        current_version = int(config_fields.get("value", 0))
        config_fields["value"] = current_version + 1
        config_fields["updated_at"] = timestamp
        writes.append(write_document("app_config", "data_version", config_fields, project))
    firestore.commit(writes)
    print(f"[{args_local.mode}] committed {len(writes)} Firestore writes")


if __name__ == "__main__":
    main()
