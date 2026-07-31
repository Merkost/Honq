#!/usr/bin/env python3
"""Validate stable question-bank sources without reading or writing Firebase."""

from __future__ import annotations

import argparse
import hashlib
import importlib.util
import json
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parent.parent
MANIFEST_PATH = Path(__file__).with_name("question-bank-manifest.json")


def load_importer():
    path = Path(__file__).with_name("import-heavy-rigid.py")
    spec = importlib.util.spec_from_file_location("question_bank_importer", path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"Unable to load importer: {path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def validate_bank(entry: dict, importer) -> dict:
    if entry.get("status") != "ready":
        return {"bank_key": entry["bank_key"], "status": entry.get("status", "source_required")}
    source = Path(entry["source_pdf"])
    if not source.exists():
        raise ValueError(f"{entry['bank_key']}: source PDF not found: {source}")
    actual_hash = hashlib.sha256(source.read_bytes()).hexdigest()
    if actual_hash != entry["source_sha256"]:
        raise ValueError(f"{entry['bank_key']}: SHA-256 mismatch: {actual_hash}")
    config = importer.configure_bank(entry["bank_key"])
    records = importer.parse_pdf(source)
    if len(records) != entry["expected_count"]:
        raise ValueError(f"{entry['bank_key']}: expected {entry['expected_count']} records, got {len(records)}")
    codes = [record.code for record in records]
    if len(codes) != len(set(codes)):
        raise ValueError(f"{entry['bank_key']}: duplicate source code")
    if entry.get("state_id") == "nt" and any("RUH" in record.text or "HVH" in record.text for record in records):
        raise ValueError(f"{entry['bank_key']}: extraction artifact in question text")
    allowed_option_counts = set(config["allowed_option_counts"])
    if any(len(record.options) not in allowed_option_counts for record in records):
        raise ValueError(f"{entry['bank_key']}: option count outside {sorted(allowed_option_counts)}")
    return {
        "bank_key": entry["bank_key"],
        "status": "valid",
        "state_id": config["state_id"],
        "question_set_id": config["question_set_id"],
        "license_type_id": config["license_type_id"],
        "count": len(records),
        "image_candidates": sum(record.has_image for record in records),
        "codes": [codes[0], codes[-1]],
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--bank", action="append", help="Validate only this bank; repeatable")
    args = parser.parse_args()
    manifest = json.loads(MANIFEST_PATH.read_text())
    selected = set(args.bank or [entry["bank_key"] for entry in manifest["banks"]])
    entries = [entry for entry in manifest["banks"] if entry["bank_key"] in selected]
    missing = selected - {entry["bank_key"] for entry in entries}
    if missing:
        raise SystemExit(f"Unknown bank(s): {', '.join(sorted(missing))}")
    importer = load_importer()
    results = [validate_bank(entry, importer) for entry in entries]
    print(json.dumps(results, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()
