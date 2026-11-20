# AirTon — Home IOP Monitoring Companion (Native Android Prototype)

> **PROTOTYPE · SIMULATION ONLY**: This application uses deterministic fictional fixtures stored locally on-device. It does **not** connect to physical hardware, actuate any air puff or optical sensor, diagnose or screen for glaucoma, display clinical thresholds, or upload patient data.

## Overview

AirTon is an Android-first, offline-first prototype demonstrating the proposed home intraocular-pressure (IOP) monitoring workflow for patients already under a treating clinician's care.

### Implemented Demo Personas (Role Preview Only — Not Real Auth)

1. **Patient / Trained Caregiver**
   - Calm home dashboard with persistent simulation banner, truthful `"No AirTon device connected"` card, read-only clinician care plan (preserving `"Not configured"` when null), latest synthetic session, and compact OD/OS trend preview.
   - 7-step **Guided Simulated Session** with pre-capture scenario picker (`Pass`, `Disconnected`, `Calibration Expired`, `Alignment`, `Blink`, `Signal Missing`, `Clock Untrusted`) and interactive Compose Canvas positioning reticle.
   - Chronological **History & Trends** keeping **OD (Right Eye)** and **OS (Left Eye)** strictly separate, displaying rejected acquisitions without numeric IOP values, and exposing all 16 `v1.0` contract provenance fields.
   - **Care Team & Care Plan** (read-only + simulated contact sheet) and **Sync & Connectivity** (offline toggle, retry preserving immutable `measurement_id`, idempotent duplicate test, and conflict replay quarantine).
2. **Treating Clinician**
   - Overview with explicit offline/delivery caveats, searchable patient roster, patient detail with separate OD/OS histories and acquisition failures.
   - **Review Queue** showing strictly `DELIVERED` + `AWAITING_REVIEW` records.
   - **Append-Only Record Review** (Acknowledge Review, Request Follow-Up, Append Clinician Note with mock author and timestamp — never overwrites prior observations).
   - **Illustrative Care-Plan Editor** preserving unresolved safety limits (thresholds, calibration intervals, escalation policy) as `"Not configured"`.
3. **Clinic Administrator / Device-Service Staff**
   - Clinic overview, patient enrolment/training & device allocation/return, device inventory (with `"Not available"` for unprovisioned fields), append-only service history log, and operational queue counts (zero fabricated revenue metrics).

### Safety & Contract Invariants

- **Strict `v1.0` Synthetic Contract**: Enforces all 16 fields, demo-prefixed IDs, `synthetic == true`, explicit ISO-8601 timezone offsets, unknown-field JSON rejection, `REJECT => iop_estimate_mmhg == null && quality_reasons.isNotEmpty()`, `PASS => quality_reasons.isEmpty() && clock_status == trusted`, and `clock_status == untrusted => REJECT + clock_untrusted`.
- **Three Independent State Machines**: Acquisition Quality (`pass`/`reject`), Transport Status (`local-only`/`pending`/`delivered`/`failed`/`quarantined`), and Clinical Review Status (`not applicable`/`awaiting review`/`reviewed`/`follow-up requested`) are tracked and rendered independently.
- **Zero Dangerous Permissions**: Requests no Bluetooth, Camera, Location, Microphone, Contacts, or Storage permissions.
- **English + Tamil Localization**: Complete English copy plus Tamil UI navigation/chrome labels with explicit `[Review Required]` tags on clinical-adjacent notices.

## Local Build & Test Commands

```bash
# Run unit, v1.0 contract, Room persistence, and Robolectric Compose UI tests
gradle :app:testDebugUnitTest

# Build debug APK
gradle :app:assembleDebug

# Run Android Lint
gradle :app:lintDebug
```

## Architecture & Integration Gates Still Blocked Before Clinical Use

All persistence, device simulation, sync, and persona controls sit behind clean Kotlin interfaces (`MeasurementRepository`, `CarePlanRepository`, `PatientRepository`, `ClinicianRepository`, `DeviceGateway`, `SyncRepository`, `ReviewRepository`, `ClinicOperationsRepository`, `DemoModeController`).

Before any real-world deployment, the following specifications and approvals are still required:

1. **Hardware & BLE GATT Protocol**: Approved device communication specification, cryptographic pairing, hardware safety interlocks, pneumatic pressure limits, and optical exposure limits.
2. **Calibrated Physical IOP Model**: Verified optical-waveform pressure estimation model and clinical validation study (never substitute retinal image classifiers).
3. **Production Cloud API & IAM**: Authenticated, encrypted, audit-logged clinical backend with real patient/clinician identity, consent, and RBAC (replacing the demo role switch).
4. **Regulatory & Clinical Governance**: Formal risk management, usability engineering, verified Tamil clinical translations, and regulatory authorization.

See [`RELEASE.md`](./RELEASE.md) for tagged Android GitHub Release instructions and signing secrets.
