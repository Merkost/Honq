#!/usr/bin/env python3
"""One-shot exporter that dumps Supabase content tables and question images
into shared/src/commonMain/composeResources/files/content/v1/ for offline
embedding. See docs/superpowers/specs/2026-04-28-supabase-removal-design.md.
"""

from __future__ import annotations

import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
LOCAL_PROPS = REPO_ROOT / "local.properties"
OUT_DIR = REPO_ROOT / "shared/src/commonMain/composeResources/files/content/v1"

# (table_name, order_clause). order_clause must use columns that exist on the
# table; the join table question_set_categories has no `id`, so it gets a
# composite key. Stable ordering keeps the JSON bundle diff-friendly.
TABLES: list[tuple[str, str]] = [
    ("states", "id"),
    ("license_types", "id"),
    ("assessment_types", "id"),
    ("categories", "id"),
    ("question_sets", "id"),
    ("question_set_categories", "question_set_id,category_id"),
    ("questions", "id"),
    ("state_resources", "id"),
]


def read_local_properties() -> dict[str, str]:
    if not LOCAL_PROPS.exists():
        sys.exit(f"local.properties not found at {LOCAL_PROPS}")
    props: dict[str, str] = {}
    for line in LOCAL_PROPS.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        props[key.strip()] = value.strip()
    return props


def http_get(url: str, headers: dict[str, str]) -> bytes:
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            return resp.read()
    except urllib.error.HTTPError as e:
        sys.exit(f"HTTP {e.code} for {url}: {e.read().decode(errors='replace')}")


def fetch_table(base_url: str, key: str, table: str, order: str) -> list[dict]:
    headers = {"apikey": key, "Authorization": f"Bearer {key}"}
    query = urllib.parse.urlencode({"select": "*", "order": order})
    body = http_get(f"{base_url}/rest/v1/{table}?{query}", headers)
    return json.loads(body.decode("utf-8"))


def main() -> None:
    props = read_local_properties()
    supabase_url = props.get("supabase.url", "").rstrip("/")
    supabase_key = props.get("supabase.key", "")
    if not supabase_url or not supabase_key:
        sys.exit("supabase.url or supabase.key missing from local.properties")

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    table_data: dict[str, list[dict]] = {}
    for table, order in TABLES:
        print(f"Fetching {table}... ", end="", flush=True)
        rows = fetch_table(supabase_url, supabase_key, table, order)
        table_data[table] = rows
        out_file = OUT_DIR / f"{table}.json"
        out_file.write_text(json.dumps(rows, indent=2, ensure_ascii=False) + "\n")
        print(f"{len(rows)} rows")

    image_paths = sorted({
        row["image_url"]
        for row in table_data["questions"]
        if isinstance(row.get("image_url"), str) and row["image_url"].strip()
    })
    print(f"Downloading {len(image_paths)} images...")

    headers = {"apikey": supabase_key}
    total_bytes = 0
    for rel_path in image_paths:
        body = http_get(
            f"{supabase_url}/storage/v1/object/public/{rel_path}",
            headers,
        )
        out_file = OUT_DIR / rel_path
        out_file.parent.mkdir(parents=True, exist_ok=True)
        out_file.write_bytes(body)
        total_bytes += len(body)
    print(f"Wrote {len(image_paths)} images, {total_bytes // 1024} KB total")
    print(f"Done. Bundle written to {OUT_DIR}")


if __name__ == "__main__":
    main()
