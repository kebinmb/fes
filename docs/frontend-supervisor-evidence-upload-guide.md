# Frontend Guide: Supervisor Faculty Evidence Upload

This guide explains how to integrate the supervisor evidence upload feature in the frontend.

The backend handles Google Drive storage automatically. The frontend only needs to send the correct faculty ID, selected criterion, optional evaluation context, and file.

## Feature Summary

Supervisors can upload evidence files for a faculty member based on faculty evaluation score criteria.

The backend stores uploaded files in Google Drive using this folder structure:

```text
GOOGLE_DRIVE_FOLDER_ID/
└── YYYY-MM-DD/
    └── FACULTY_ID/
        └── uploaded-evidence-file
```

Example:

```text
Faculty Evaluation Evidences/
└── 2026-06-29/
    └── FAC-0001/
        └── 4e3ab3c1-3d0c-4991-aef0.pdf
```

The frontend does not need to create these folders. Folder creation is handled by the backend.

## Backend Requirements

Make sure the backend has these environment variables configured:

```env
EVIDENCE_STORAGE_PROVIDER=google-drive
GOOGLE_DRIVE_CREDENTIALS_PATH=C:/path/to/service-account.json
GOOGLE_DRIVE_FOLDER_ID=your_google_drive_folder_id
```

The Google Drive folder must be shared with the service account email from the JSON credentials file.

Do not put Google Drive credentials in the frontend.

## API Base URL

The backend API uses this context path:

```text
/api
```

Evidence endpoints:

```text
GET    /api/faculty/evidences/criteria
POST   /api/faculty/evidences
GET    /api/faculty/evidences?facultyId={facultyId}
GET    /api/faculty/evidences/{evidenceId}/download
DELETE /api/faculty/evidences/{evidenceId}
```

## API Details

### Get Evidence Criteria

Use this endpoint to display the criteria supervisors can attach evidence to.

```http
GET /api/faculty/evidences/criteria
```

Response:

```json
[
  {
    "name": "PUNCTUALITY",
    "category": "Management of Teaching and Learning",
    "label": "Punctuality",
    "ratingKey": "punctuality"
  }
]
```

Use `name` as the upload value for `criterion`.

### Upload Evidence

```http
POST /api/faculty/evidences
Content-Type: multipart/form-data
```

Form fields:

| Field | Required | Description |
| --- | --- | --- |
| `facultyId` | Yes | Faculty ID where evidence belongs |
| `criterion` | Yes | Criterion name, for example `PUNCTUALITY` |
| `file` | Yes | Evidence file |
| `classCode` | No | Related class code |
| `subjectCode` | No | Related subject code |
| `yearLevel` | No | Related year level |
| `semester` | No | Related semester |
| `schoolYear` | No | Related school year |
| `description` | No | Supervisor notes |

Response:

```json
{
  "evidenceId": 1,
  "facultyId": "FAC-0001",
  "classCode": "BSIT-3A",
  "subjectCode": "IT101",
  "yearLevel": "3rd Year",
  "semester": "1st Semester",
  "schoolYear": 2026,
  "criterion": "PUNCTUALITY",
  "criterionCategory": "Management of Teaching and Learning",
  "criterionLabel": "Punctuality",
  "description": "Attendance and class start-time documentation.",
  "uploadedBy": "supervisor@example.com",
  "originalFilename": "attendance-proof.pdf",
  "contentType": "application/pdf",
  "fileSize": 128000,
  "createdAt": "2026-06-29T03:10:00Z"
}
```

### List Faculty Evidence

```http
GET /api/faculty/evidences?facultyId=FAC-0001&page=0&size=10&sort=createdAt,desc
```

Optional filters:

```text
classCode
subjectCode
semester
schoolYear
criterion
```

Response is a Spring pageable response:

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 10,
  "number": 0
}
```

### Download Evidence

```http
GET /api/faculty/evidences/{evidenceId}/download
```

This returns a file blob.

### Delete Evidence

```http
DELETE /api/faculty/evidences/{evidenceId}
```

Response:

```http
204 No Content
```

## Angular Models

Create:

```text
src/app/models/faculty-evidence.model.ts
```

```ts
export interface EvidenceCriterion {
  name: string;
  category: string;
  label: string;
  ratingKey: string;
}

export interface FacultyEvidence {
  evidenceId: number;
  facultyId: string;
  classCode?: string;
  subjectCode?: string;
  yearLevel?: string;
  semester?: string;
  schoolYear?: number;
  criterion: string;
  criterionCategory: string;
  criterionLabel: string;
  description?: string;
  uploadedBy: string;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
```

## Angular Service

Create:

```text
src/app/services/faculty-evidence.service.ts
```

```ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  EvidenceCriterion,
  FacultyEvidence,
  PageResponse,
} from '../models/faculty-evidence.model';

@Injectable({ providedIn: 'root' })
export class FacultyEvidenceService {
  private readonly baseUrl = '/api/faculty/evidences';

  constructor(private http: HttpClient) {}

  getCriteria() {
    return this.http.get<EvidenceCriterion[]>(`${this.baseUrl}/criteria`);
  }

  uploadEvidence(payload: {
    facultyId: string;
    criterion: string;
    file: File;
    classCode?: string;
    subjectCode?: string;
    yearLevel?: string;
    semester?: string;
    schoolYear?: number;
    description?: string;
  }) {
    const formData = new FormData();

    formData.append('facultyId', payload.facultyId);
    formData.append('criterion', payload.criterion);
    formData.append('file', payload.file);

    if (payload.classCode) formData.append('classCode', payload.classCode);
    if (payload.subjectCode) formData.append('subjectCode', payload.subjectCode);
    if (payload.yearLevel) formData.append('yearLevel', payload.yearLevel);
    if (payload.semester) formData.append('semester', payload.semester);
    if (payload.schoolYear) formData.append('schoolYear', String(payload.schoolYear));
    if (payload.description) formData.append('description', payload.description);

    return this.http.post<FacultyEvidence>(this.baseUrl, formData);
  }

  getEvidenceList(filters: {
    facultyId: string;
    classCode?: string;
    subjectCode?: string;
    semester?: string;
    schoolYear?: number;
    criterion?: string;
    page?: number;
    size?: number;
  }) {
    let params = new HttpParams()
      .set('facultyId', filters.facultyId)
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 10))
      .set('sort', 'createdAt,desc');

    if (filters.classCode) params = params.set('classCode', filters.classCode);
    if (filters.subjectCode) params = params.set('subjectCode', filters.subjectCode);
    if (filters.semester) params = params.set('semester', filters.semester);
    if (filters.schoolYear) params = params.set('schoolYear', String(filters.schoolYear));
    if (filters.criterion) params = params.set('criterion', filters.criterion);

    return this.http.get<PageResponse<FacultyEvidence>>(this.baseUrl, { params });
  }

  downloadEvidence(evidenceId: number) {
    return this.http.get(`${this.baseUrl}/${evidenceId}/download`, {
      responseType: 'blob',
      observe: 'response',
    });
  }

  deleteEvidence(evidenceId: number) {
    return this.http.delete<void>(`${this.baseUrl}/${evidenceId}`);
  }
}
```

Important: do not manually set the `Content-Type` header when uploading `FormData`. The browser must set the multipart boundary.

## Component Example

Create a supervisor evidence upload component or add this to your existing faculty evaluation details page.

```ts
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import {
  EvidenceCriterion,
  FacultyEvidence,
} from '../../models/faculty-evidence.model';
import { FacultyEvidenceService } from '../../services/faculty-evidence.service';

@Component({
  selector: 'app-faculty-evidence-upload',
  templateUrl: './faculty-evidence-upload.component.html',
})
export class FacultyEvidenceUploadComponent implements OnInit {
  @Input({ required: true }) facultyId!: string;

  criteria: EvidenceCriterion[] = [];
  evidences: FacultyEvidence[] = [];
  selectedFile?: File;
  isUploading = false;
  errorMessage = '';

  form = this.fb.group({
    criterion: ['', Validators.required],
    classCode: [''],
    subjectCode: [''],
    yearLevel: [''],
    semester: [''],
    schoolYear: [new Date().getFullYear()],
    description: [''],
  });

  constructor(
    private fb: FormBuilder,
    private evidenceService: FacultyEvidenceService
  ) {}

  ngOnInit() {
    this.loadCriteria();
    this.loadEvidences();
  }

  loadCriteria() {
    this.evidenceService.getCriteria().subscribe({
      next: criteria => {
        this.criteria = criteria;
      },
      error: () => {
        this.errorMessage = 'Unable to load evidence criteria.';
      },
    });
  }

  loadEvidences() {
    this.evidenceService
      .getEvidenceList({
        facultyId: this.facultyId,
      })
      .subscribe({
        next: page => {
          this.evidences = page.content;
        },
        error: () => {
          this.errorMessage = 'Unable to load evidence records.';
        },
      });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      this.selectedFile = undefined;
      return;
    }

    const allowedTypes = [
      'application/pdf',
      'image/jpeg',
      'image/png',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    ];

    if (!allowedTypes.includes(file.type)) {
      this.errorMessage = 'Only PDF, image, and Word files are allowed.';
      input.value = '';
      this.selectedFile = undefined;
      return;
    }

    const maxSizeMb = 15;
    if (file.size > maxSizeMb * 1024 * 1024) {
      this.errorMessage = `File must not exceed ${maxSizeMb}MB.`;
      input.value = '';
      this.selectedFile = undefined;
      return;
    }

    this.errorMessage = '';
    this.selectedFile = file;
  }

  upload() {
    if (this.form.invalid || !this.selectedFile) {
      this.errorMessage = 'Please select a criterion and evidence file.';
      return;
    }

    this.isUploading = true;
    this.errorMessage = '';

    const value = this.form.value;

    this.evidenceService
      .uploadEvidence({
        facultyId: this.facultyId,
        criterion: value.criterion!,
        file: this.selectedFile,
        classCode: value.classCode || undefined,
        subjectCode: value.subjectCode || undefined,
        yearLevel: value.yearLevel || undefined,
        semester: value.semester || undefined,
        schoolYear: value.schoolYear || undefined,
        description: value.description || undefined,
      })
      .subscribe({
        next: () => {
          this.isUploading = false;
          this.selectedFile = undefined;
          this.form.patchValue({
            criterion: '',
            description: '',
          });
          this.loadEvidences();
        },
        error: () => {
          this.isUploading = false;
          this.errorMessage = 'Evidence upload failed.';
        },
      });
  }

  download(evidence: FacultyEvidence) {
    this.evidenceService.downloadEvidence(evidence.evidenceId).subscribe({
      next: response => {
        const blob = response.body;
        if (!blob) return;

        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');

        link.href = url;
        link.download = evidence.originalFilename;
        link.click();

        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.errorMessage = 'Evidence download failed.';
      },
    });
  }

  deleteEvidence(evidenceId: number) {
    if (!confirm('Delete this evidence file?')) return;

    this.evidenceService.deleteEvidence(evidenceId).subscribe({
      next: () => {
        this.loadEvidences();
      },
      error: () => {
        this.errorMessage = 'Unable to delete evidence.';
      },
    });
  }

  groupedCriteria() {
    return this.criteria.reduce<Record<string, EvidenceCriterion[]>>(
      (groups, criterion) => {
        groups[criterion.category] ??= [];
        groups[criterion.category].push(criterion);
        return groups;
      },
      {}
    );
  }
}
```

## Template Example

```html
<section class="evidence-panel">
  <h2>Faculty Evidence</h2>

  <p *ngIf="errorMessage" class="error">
    {{ errorMessage }}
  </p>

  <form [formGroup]="form" (ngSubmit)="upload()">
    <label>
      Criterion
      <select formControlName="criterion">
        <option value="">Select criterion</option>
        <ng-container *ngFor="let group of groupedCriteria() | keyvalue">
          <optgroup [label]="group.key">
            <option *ngFor="let item of group.value" [value]="item.name">
              {{ item.label }}
            </option>
          </optgroup>
        </ng-container>
      </select>
    </label>

    <label>
      Class Code
      <input type="text" formControlName="classCode" />
    </label>

    <label>
      Subject Code
      <input type="text" formControlName="subjectCode" />
    </label>

    <label>
      Year Level
      <input type="text" formControlName="yearLevel" />
    </label>

    <label>
      Semester
      <input type="text" formControlName="semester" />
    </label>

    <label>
      School Year
      <input type="number" formControlName="schoolYear" />
    </label>

    <label>
      Description
      <textarea formControlName="description"></textarea>
    </label>

    <label>
      Evidence File
      <input
        type="file"
        accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
        (change)="onFileSelected($event)"
      />
    </label>

    <button type="submit" [disabled]="form.invalid || !selectedFile || isUploading">
      {{ isUploading ? 'Uploading...' : 'Upload Evidence' }}
    </button>
  </form>

  <table>
    <thead>
      <tr>
        <th>Criterion</th>
        <th>File</th>
        <th>Uploaded By</th>
        <th>Date</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr *ngFor="let evidence of evidences">
        <td>
          <strong>{{ evidence.criterionLabel }}</strong>
          <small>{{ evidence.criterionCategory }}</small>
        </td>
        <td>{{ evidence.originalFilename }}</td>
        <td>{{ evidence.uploadedBy }}</td>
        <td>{{ evidence.createdAt | date: 'medium' }}</td>
        <td>
          <button type="button" (click)="download(evidence)">Download</button>
          <button type="button" (click)="deleteEvidence(evidence.evidenceId)">
            Delete
          </button>
        </td>
      </tr>
    </tbody>
  </table>
</section>
```

## Suggested UI Placement

Best placement:

```text
Supervisor Dashboard
└── Faculty Evaluation
    └── Faculty Details
        └── Evidence Upload Tab
```

Recommended sections:

1. Faculty information summary
2. Evidence upload form
3. Uploaded evidence table
4. Filters by criterion, subject, semester, and school year

## Frontend Validation

Recommended client-side validation:

| Rule | Recommendation |
| --- | --- |
| Required fields | `facultyId`, `criterion`, `file` |
| File types | PDF, JPG, PNG, DOC, DOCX |
| File size | 15MB or lower |
| Criterion | Must come from backend `/criteria` endpoint |
| Faculty ID | Use the selected faculty record ID |

The backend still validates required fields, so frontend validation is for better user experience only.

## Authentication

The backend uses the logged-in user from Spring Security as `uploadedBy`.

That means the frontend must include the normal authentication token or session credentials already used in the rest of the app.

If your app uses JWT, make sure the interceptor attaches the token:

```ts
Authorization: Bearer <token>
```

## Common Mistakes

Avoid these:

- Do not upload directly to Google Drive from the frontend.
- Do not expose the service account JSON file.
- Do not set `Content-Type: multipart/form-data` manually.
- Do not hard-code criteria in the frontend.
- Do not send `criterionLabel`; send `criterion`.
- Do not send Drive folder IDs from the frontend.

## Manual Test Checklist

Use this checklist after implementing the frontend:

```text
[ ] Criteria dropdown loads from backend.
[ ] Supervisor can select a faculty member.
[ ] Supervisor can select a criterion.
[ ] Supervisor can choose a valid file.
[ ] Upload button is disabled until required fields are complete.
[ ] Upload succeeds and refreshes the evidence table.
[ ] Evidence appears under the correct faculty ID.
[ ] Google Drive contains YYYY-MM-DD/FACULTY_ID/file.
[ ] Download button downloads the original file.
[ ] Delete button removes the evidence record and Drive file.
[ ] Invalid file type is rejected by the frontend.
[ ] Large file is rejected by the frontend.
```

## Example Upload Request With Fetch

If you are not using Angular, the same upload works with plain `fetch`:

```ts
async function uploadEvidence(file: File) {
  const formData = new FormData();

  formData.append('facultyId', 'FAC-0001');
  formData.append('criterion', 'PUNCTUALITY');
  formData.append('description', 'Class attendance evidence.');
  formData.append('file', file);

  const response = await fetch('/api/faculty/evidences', {
    method: 'POST',
    body: formData,
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`,
    },
  });

  if (!response.ok) {
    throw new Error('Evidence upload failed.');
  }

  return response.json();
}
```

Do not add a `Content-Type` header here. The browser will add it automatically.
