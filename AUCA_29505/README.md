# AUCA_ID – School Management System (Java console application)

## Run
- Linux/macOS: `./run.sh`   Windows: `run.bat`   (JDK 17+; tested on JDK 21)
- Manual: `javac -d out $(find src -name '*.java')` then `java -cp out com.auca.main.Main`
- Optional scripted demo: `java -cp out com.auca.main.Main < demo_input.txt`

## Structure (root package `com.auca`)
| Package | Types |
|---|---|
| `interfaces` | `Identifiable`, `Registrable` (6 constants each; `Registrable` also holds shared static validators) |
| `abstracts` | `Person` (implements both), `AcademicMember` (extends Person) |
| `student`, `teacher` | extend `AcademicMember` (multilevel) |
| `administrator` | extends `Person` (hierarchical) |
| `department`, `course`, `classroom`, `enrollment`, `assessment`, `feepayment`, `librarybook`, `bookloan` | implement both interfaces directly |
| `exceptions` | `ValidationException` (checked), `DuplicateRecordException`, `RecordNotFoundException`, `PrerequisiteMissingException`, `BookUnavailableException` (runtime) |
| `main` | `Main` |

15 domain types = 2 interfaces + 2 abstract + 11 concrete, exactly as specified, each with its six mandatory attributes.

## Design decisions to explain at the demonstration
- **Interface rule / abstract methods:** `getRoleDescription()` is abstract in `Person` and `getAcademicCategory()` is abstract in `AcademicMember` (the "cleaner alternative" in section 4), so non-person entities are not forced to implement them.
- **Why Person/AcademicMember can't be instantiated:** they are `abstract` and declare abstract methods; only complete subclasses can be created.
- **Constructor chaining / super / overriding:** `Student -> AcademicMember -> Person` constructors call `super(...)`; `validate()` and `printDetails()` are overridden and call `super.` first.
- **Shared static validators** live in `Registrable` (static interface methods), so all validation rules are declared in an interface. `Main` has only private helpers + `main`.
- **One generic form engine** (`Main.workflow`) runs all 11 four-step forms from field tables: steps 1-3 collect fields, step 4 reviews (Save / Edit field / Cancel). The same engine powers *Update Record* (with rollback if validation fails).
- **Missing prerequisites** are detected at the moment the dependent field is reached: the user may register the prerequisite (nested form) and the original form resumes with all entered values intact; "Return to menu" throws `PrerequisiteMissingException` and nothing is saved.
- **Stock:** `availableCopies` is decremented once, only inside the successful save of a loan, and incremented once on a confirmed return.
- **Documented rules:** study year 1-6 (section 8; the sample form in section 17 says 1-8 - section 8 is treated as the binding validation rule). Join/hire/admission/enrollment/payment/assessment dates may not be in the future. Money has at most 2 decimals. Semester values: FALL/SPRING/SUMMER.
- Allowed values: academic status ACTIVE/INACTIVE; employment type FULL_TIME/PART_TIME/CONTRACT; enrollment status ACTIVE/COMPLETED/DROPPED; assessment type QUIZ/ASSIGNMENT/MIDTERM/FINAL_EXAM/PROJECT; payment method CASH/CARD/MOBILE_MONEY/BANK_TRANSFER.
- Data is in-memory (ArrayLists); it is lost when the program exits.
