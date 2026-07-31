# Australian question-bank source review

## Stable sources currently approved for import

- NSW: official downloadable car, rider, rider-SMV, rigid, and combination publications.
- NT: official downloadable Class C, Class R, rigid, and articulated PDFs. The current hashes and source-code counts are recorded in `scripts/question-bank-manifest.json`.

## Sources requiring acquisition before activation

QLD, VIC, WA, SA, TAS, and ACT are represented in the manifest as `source_required`. Their official resources are practice-test, sample, handbook, or legislation surfaces rather than a stable full question-bank file suitable for bulk import. Do not scrape randomized practice sessions or activate a placeholder qset.

Before adding one of these states, record the supplied PDF/JSON or licensed content source, its SHA-256, redistribution permission, extraction review, answer review, image policy, and the qset/license mapping in the manifest. Keep the qset inactive until those checks pass.
