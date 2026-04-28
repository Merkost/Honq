#!/usr/bin/env python3
"""
One-shot exporter: Supabase Postgres + Storage → Firestore + Firebase Hosting (public/questions).

Reads credentials from local.properties (same file the app uses):
  supabase.url, supabase.service.role.key (or supabase.key), firebase.project.id

Requires GOOGLE_APPLICATION_CREDENTIALS env var pointing at a Firebase service-account JSON
(download from Firebase Console → Project Settings → Service accounts → Generate new private key).
The service account is used ONLY for Firestore writes; image staging is plain filesystem writes
into public/questions/ and the actual Hosting deploy is done separately via the firebase CLI
(which uses your interactive login, not this service account).

Usage:
  GOOGLE_APPLICATION_CREDENTIALS=/path/to/sa.json python scripts/export-supabase-to-firestore.py

After the script completes, publish the images:
  firebase deploy --only hosting --project <firebase.project.id>

Idempotent: re-running overwrites the same docs and re-stages the same image files.
"""

import os
import sys
from pathlib import Path

import firebase_admin
from firebase_admin import credentials, firestore
from supabase import Client, create_client

REPO_ROOT = Path(__file__).resolve().parent.parent
LOCAL_PROPERTIES = REPO_ROOT / "local.properties"
HOSTING_QUESTIONS_DIR = REPO_ROOT / "public" / "questions"

TABLES = [
    "states",
    "license_types",
    "assessment_types",
    "categories",
    "question_sets",
    "question_set_categories",
    "questions",
    "state_resources",
    "app_config",
]
SUPABASE_BUCKET = "questions"


def load_local_properties() -> dict[str, str]:
    if not LOCAL_PROPERTIES.exists():
        sys.exit(f"local.properties not found at {LOCAL_PROPERTIES}")
    out: dict[str, str] = {}
    for raw in LOCAL_PROPERTIES.read_text().splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        out[k.strip()] = v.strip()
    return out


def doc_id_for(table: str, row: dict) -> str:
    # app_config keyed by "key"; question_set_categories has a composite key; everything else by "id".
    if table == "app_config":
        return str(row["key"])
    if table == "question_set_categories":
        return f"{row['question_set_id']}__{row['category_id']}"
    return str(row["id"])


def export_tables(supabase: Client, fs: firestore.Client) -> None:
    for table in TABLES:
        print(f"[tables] exporting {table}...")
        offset = 0
        page = 1000
        total = 0
        while True:
            resp = supabase.table(table).select("*").range(offset, offset + page - 1).execute()
            rows = resp.data or []
            if not rows:
                break
            batch = fs.batch()
            for row in rows:
                doc_ref = fs.collection(table).document(doc_id_for(table, row))
                batch.set(doc_ref, row)
            batch.commit()
            total += len(rows)
            if len(rows) < page:
                break
            offset += page
        print(f"[tables] {table}: {total} docs written")


def stage_images(supabase: Client) -> None:
    print(f"[hosting] listing supabase bucket '{SUPABASE_BUCKET}'...")
    listing = supabase.storage.from_(SUPABASE_BUCKET).list(path="", options={"limit": 5000})
    if not listing:
        print("[hosting] empty bucket — skipping")
        return
    HOSTING_QUESTIONS_DIR.mkdir(parents=True, exist_ok=True)
    count = 0
    for entry in listing:
        name = entry["name"]
        if name.endswith("/"):
            continue
        print(f"[hosting] staging {name}...")
        blob_bytes = supabase.storage.from_(SUPABASE_BUCKET).download(name)
        out_path = HOSTING_QUESTIONS_DIR / name
        out_path.parent.mkdir(parents=True, exist_ok=True)
        out_path.write_bytes(blob_bytes)
        count += 1
    print(f"[hosting] staged {count} files in {HOSTING_QUESTIONS_DIR}")
    print(f"[hosting] now run: firebase deploy --only hosting --project <firebase.project.id>")


def main() -> None:
    props = load_local_properties()
    supabase_url = props.get("supabase.url")
    supabase_key = props.get("supabase.service.role.key") or props.get("supabase.key")
    fb_project_id = props.get("firebase.project.id")

    missing = [k for k, v in {
        "supabase.url": supabase_url,
        "supabase.service.role.key (or supabase.key)": supabase_key,
        "firebase.project.id": fb_project_id,
    }.items() if not v]
    if missing:
        sys.exit("Missing required local.properties keys: " + ", ".join(missing))

    if "GOOGLE_APPLICATION_CREDENTIALS" not in os.environ:
        sys.exit("Set GOOGLE_APPLICATION_CREDENTIALS to a Firebase service-account JSON path")

    print(f"Source: {supabase_url}")
    print(f"Dest:   firestore project={fb_project_id}; images → {HOSTING_QUESTIONS_DIR}")

    supabase = create_client(supabase_url, supabase_key)
    cred = credentials.ApplicationDefault()
    firebase_admin.initialize_app(cred, {"projectId": fb_project_id})
    fs = firestore.client()

    export_tables(supabase, fs)
    stage_images(supabase)
    print("Done. Run `firebase deploy --only hosting` to publish images.")


if __name__ == "__main__":
    main()
