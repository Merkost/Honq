#!/usr/bin/env python3
"""Parser tests for the state-specific question-bank importer."""

from __future__ import annotations

import importlib.util
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parent.parent
SPEC = importlib.util.spec_from_file_location("question_bank_importer", ROOT / "scripts" / "import-heavy-rigid.py")
if SPEC is None or SPEC.loader is None:
    raise RuntimeError("Unable to load question-bank importer")
IMPORTER = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = IMPORTER
SPEC.loader.exec_module(IMPORTER)


class QuestionBankParserTest(unittest.TestCase):
    def test_nt_sources_have_expected_source_code_counts(self) -> None:
        expected = {"nt_car": 358, "nt_rider": 350, "nt_rigid": 289, "nt_articulated": 298}
        for bank, count in expected.items():
            with self.subTest(bank=bank):
                config = IMPORTER.configure_bank(bank)
                records = IMPORTER.parse_pdf(config["pdf"])
                self.assertEqual(count, len(records))
                self.assertEqual(len(records), len({record.code for record in records}))

    def test_nt_records_have_three_options_and_one_answer(self) -> None:
        for bank in ("nt_car", "nt_rider", "nt_rigid", "nt_articulated"):
            with self.subTest(bank=bank):
                config = IMPORTER.configure_bank(bank)
                records = IMPORTER.parse_pdf(config["pdf"])
                self.assertTrue(all(len(record.options) == 3 for record in records))
                self.assertTrue(all(0 <= record.correct_index < 3 for record in records))
                self.assertTrue(all(record.category in IMPORTER.PREFIX_TO_CATEGORY.values() for record in records))

    def test_state_and_license_configuration(self) -> None:
        for bank in ("nt_car", "nt_rider", "nt_rigid", "nt_articulated"):
            with self.subTest(bank=bank):
                config = IMPORTER.configure_bank(bank)
                self.assertEqual("nt", config["state_id"])
                self.assertTrue(config["question_set_id"].startswith("nt_"))


if __name__ == "__main__":
    unittest.main()
