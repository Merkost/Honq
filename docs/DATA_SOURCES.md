# Data Sources for Driving Test Questions

## NSW (New South Wales) - Primary Target

### Official Sources

| Resource | Link | Description |
|----------|------|-------------|
| **Road User Handbook** | [PDF Download](https://www.nsw.gov.au/sites/default/files/2022-11/Road-User-Handbook-English.pdf) | Official handbook (7MB PDF) covering all road rules |
| **DKT Official Page** | [NSW Government](https://www.nsw.gov.au/driving-boating-and-transport/driver-and-rider-licences/driver-licences/driver-licence-tests/driver-knowledge-test) | Official test information |
| **DKT Question Bank** | `rms.nsw.gov.au/documents/roads/licence/driver-knowledge-test-questions-car.pdf` | All 364 official questions (check Transport NSW for current URL) |
| **Service NSW** | [DKT Practice](https://www.service.nsw.gov.au/referral/road-user-handbook) | Official practice tests |

### Test Structure
- **Total Questions**: 45 (from bank of 364)
- **Part 1**: 15 general knowledge questions (need 12+ correct)
- **Part 2**: 30 road safety questions (need 29+ correct)
- **Format**: Multiple choice with 3 options each
- **Time Limit**: Untimed for practice; timed for actual test

### Third-Party Sources (for reference/validation)
| Source | Link | Notes |
|--------|------|-------|
| DrivingTestNSW | [All 364 Questions](https://www.drivingtestnsw.com/driver-knowledge-test-all-364-questions/) | Community-maintained question bank |
| EzLicence | [NSW Practice](https://www.ezlicence.com.au/learners-tests/nsw) | Free practice tests |
| DKT Test | [NSW Practice](https://dkttest.com/new-south-wales/) | Practice platform |

---

## Victoria (VIC) - Future Expansion

### Official Sources
| Resource | Link | Description |
|----------|------|-------------|
| **Road to Solo Driving Handbook** | [VicRoads](https://www.vicroads.vic.gov.au/licences/your-ls/learner-permit-test-online) | Official study material |
| **Practice Test** | [VicRoads Practice](https://www.vicroads.vic.gov.au/licences/your-ls/learner-permit-test-inperson/lpt) | Official practice test |
| **Offline Practice** | [Downloadable Sheet](https://www.vicroads.vic.gov.au/licences/your-ls/learner-permit-test-inperson/lpt/lptoffline) | Offline practice version |

### Test Structure
- **Total Questions**: 32
- **Pass Mark**: 25 correct (78%)
- **Age Requirement**: 16+

---

## Queensland (QLD) - Future Expansion

### Official Sources
| Resource | Link | Description |
|----------|------|-------------|
| **Your Keys to Driving** | Queensland Transport | Official handbook |
| **TMR Written Test** | Transport and Main Roads | Official test information |

---

## Other States

| State | Authority | Handbook |
|-------|-----------|----------|
| **SA** | Service SA | South Australian Road Rules |
| **WA** | Department of Transport | Drive Safe handbook |
| **TAS** | Service Tasmania | Tasmanian Road Rules |
| **NT** | Motor Vehicle Registry | Road Users Handbook |
| **ACT** | Access Canberra | ACT Road Rules Handbook |

---

## Data Extraction Strategy

### Method 1: Manual Extraction (Recommended for accuracy)
1. Download official PDF question banks
2. Parse and structure into JSON/CSV
3. Categorize by topic (Road Rules, Signs, Safety, etc.)
4. Add metadata (difficulty, source, version)
5. Review for accuracy

### Method 2: Web Scraping (Supplementary)
1. Scrape third-party practice test sites
2. Cross-reference with official sources
3. Validate answers against handbook
4. **Note**: Respect robots.txt and terms of service

### Method 3: Community Contribution
1. Allow users to submit/verify questions
2. Moderation workflow for quality control
3. Attribution and source tracking

---

## Question Categories (NSW)

Based on Road User Handbook structure:

| Category ID | Name | Topics Covered |
|-------------|------|----------------|
| `road_rules` | Road Rules | Give way, lane usage, overtaking |
| `road_signs` | Road Signs | Regulatory, warning, information signs |
| `speed_limits` | Speed Limits | Speed zones, variable limits |
| `intersections` | Intersections | Roundabouts, T-intersections, traffic lights |
| `alcohol_drugs` | Alcohol & Drugs | BAC limits, drug driving |
| `safety` | Safety | Seatbelts, child restraints, fatigue |
| `hazards` | Hazard Perception | Identifying and responding to hazards |
| `passengers` | Passengers & Load | Passenger rules, load securing |
| `parking` | Parking | Parking rules, stopping, standing |
| `emergency` | Emergency | Emergency vehicles, breakdowns |

---

## Data Quality Checklist

- [ ] Source is official or verified against official source
- [ ] Question text matches exactly (or paraphrased with same meaning)
- [ ] All answer options are correct
- [ ] Correct answer index is verified
- [ ] Explanation is accurate and helpful
- [ ] Category is appropriate
- [ ] Image (if any) is properly attributed
- [ ] Version/last updated date tracked

---

## Legal Considerations

1. **Copyright**: Official questions may be Crown copyright
2. **Fair Use**: Educational use generally permitted
3. **Attribution**: Always credit source (Transport NSW, VicRoads, etc.)
4. **Updates**: Monitor for changes to official materials
5. **Disclaimer**: App should state it's for practice only, not official test

---

## Recommended Approach for Honq

### Phase 1: NSW Launch
1. Download Road User Handbook PDF
2. Download DKT Question Bank PDF (if available)
3. Manually extract and structure 364 questions
4. Categorize into 8-10 topics
5. Add explanations from handbook
6. Review and validate

### Phase 2: Expand to VIC
1. Obtain Road to Solo Driving handbook
2. Extract VicRoads practice questions
3. Map to common category structure
4. Adjust for state-specific rules

### Phase 3: Other States
1. Prioritize by market size (QLD, WA, SA)
2. Follow same extraction process
3. Identify common vs state-specific questions
