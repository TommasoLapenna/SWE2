# Changes

Every change made during the code/essay review (2026-11-16).
Verification: `src/main` compiles with Java 25 and `Main` runs. Its output is identical to
before except for the two step titles fixed in change 1. `essay/main.tex` compiles with pdflatex
(built in a temp dir, `essay/main.pdf` was **not** regenerated).

---

## Code

### 1. Leftover text from a broken find/replace: `main.main.build` → `build`
At some point the word `build` was replaced with `main.main.build` everywhere, including in
plain text. Restored to `build` in:

| File | Line | Before | After |
|---|---|---|---|
| `src/main/application/DefaultRules.java` | 296 | `"... low efficiency rating for a main.main.build of this wattage"` | `"... low efficiency rating for a build of this wattage"` *(user-visible message of rule 40 PSU-EFFICIENCY-LOW-FOR-LOAD)* |
| `src/main/Main.java` | 34 | `step("1. Empty main.main.build: ...")` | `step("1. Empty build: ...")` |
| `src/main/Main.java` | 102 | `step("Final main.main.build")` | `step("Final build")` |
| `src/main/application/PcBuildSession.java` | 21 | `// checkout = new Checkout(main.main.build);` | `// checkout = new Checkout(build);` |
| `src/main/domain/model/BuildView.java` | 10 | `//... read only version of the main.main.build` | `//... read only version of the build` |
| `src/main/domain/model/PcBuilder.java` | 40 | `// Return a read-only view of the pc main.main.build` | `// Return a read-only view of the pc build` |

### 2. `PcBuildSession.canCheckout()`: printed raw templates and skipped warnings
`src/main/application/PcBuildSession.java`

Before:
```java
public boolean canCheckout() {
   return getViolations().stream()
           .peek(v -> { if (v.severity() == Severity.WARNING) System.out.println(v.message());})
           .noneMatch(v -> v.severity() == Severity.ERROR);
}
```
After:
```java
public boolean canCheckout() {
    List<Violation> violations = getViolations();
    // Print every warning first (with placeholders resolved), then decide: a short-circuiting
    // stream with peek() would stop printing at the first ERROR
    for (Violation v : violations)
        if (v.severity() == Severity.WARNING)
            System.out.println(v.getMessage());
    return violations.stream().noneMatch(v -> v.severity() == Severity.ERROR);
}
```
Bugs fixed:
- `v.message()` is the template, so the user saw `CPU {CPU} ships without a cooler...`.
  `v.getMessage()` resolves the placeholders (`CPU AMD Ryzen 7 7700X ships without...`).
- `noneMatch` short-circuits: once it hit the first ERROR it stopped, so `peek` never printed
  the warnings after it.

The return value is unchanged.

### 3. `Cooler.powerDrawWatts()` ignored the record's own field
`src/main/domain/model/component/Cooler.java`: removed
```java
@Override
public int powerDrawWatts() { return 5; }
```
`Cooler` declares `int powerDrawWatts` as a record component, but the override always returned
`5`, so the constructor argument was silently thrown away. Now the generated accessor returns
the value passed in (same pattern as `Case` and `Peripherals`). `Main` passes `5`, so the
output is unchanged.
→ The class diagram `Model.component.png` still says `+ powerDrawWatts(): int = 5` for
`Cooler`. That line should be removed when you re-export the diagram.

### 4. `Specs`: specification constants made `final`
`src/main/domain/rule/specification/Specs.java`: all 30
`public static BuildSpecification xxxSpecification = ...` → `public static final BuildSpecification xxxSpecification = ...`.
They were public mutable globals: any class could reassign e.g. `Specs.socketMatchSpecification`
after `DefaultRules` had captured it. Nothing reassigned them, so behaviour is unchanged.

### 5. `MemoryTest`: mixed JUnit 4 and JUnit 5
`src/test/model/component/MemoryTest.java`
- `import org.junit.Assert;` → `import org.junit.jupiter.api.Assertions;`
- `Assert.assertEquals(16, ...)` → `Assertions.assertEquals(16, ...)`

The test used JUnit 5 `@Test`/`@BeforeEach` with JUnit 4 `Assert`. Now it is JUnit 5 only, which
matches `PcBuilderTest` and the essay ("JUnit 5"). *Not run here: the JUnit 5 jars are not in
the local `~/.m2`.*

---

## Essay (`essay/main.tex`)

Only statements that contradicted the code were changed. TODOs and missing sections were not touched.

### 6. UC2 — Aggiungi componente
- Basic flow, steps 3–4 swapped to match the code (`history.save()` runs **before**
  `build.addComponent(c)`):
  - before: `3. Il sistema aggiunge il componente tramite PcBuildSession.add(). 4. Il sistema salva uno snapshot ...`
  - after: `3. Tramite PcBuildSession.add() il sistema salva uno snapshot dello stato attuale nella SnapshotHistory «include UC-Snapshot». 4. Il sistema aggiunge il componente alla build.`
- Alternative flow 3a replaced. It said the add is refused when the maximum cardinality is
  reached, but the code always adds and flags it (the essay itself says so in §Model):
  - before: `3a. La cardinalità massima per quel tipo è già raggiunta: il sistema mostra un errore e non aggiunge il componente.`
  - after: `4a. La cardinalità massima per quel tipo è già raggiunta (es. una seconda CPU): il componente viene comunque aggiunto, ma la verifica segnala una violazione di gravità ERROR (es. TOO-MANY-CPUS) che impedisce il checkout finché persiste.`

### 7. UC3 — Rimuovi componente
- Basic flow, steps 3–4 swapped (same reason as UC2):
  - after: `3. Tramite PcBuildSession.remove() il sistema salva uno snapshot dello stato attuale «include UC-Snapshot». 4. Il sistema rimuove il componente dalla build.`
- Alternative flow 3a:
  - before: `Il componente non è presente nella build: il sistema mostra un errore.`
  - after: `Il componente non è presente nella build: remove() restituisce false, nessuno snapshot viene salvato e la build resta invariata.` (this is what the code does)

### 8. §Domain intro
- before: `... nei corrispettivi pacchetti .\textit{model} e \textit{rule}`
- after: `... nei corrispettivi pacchetti \textit{model} e \textit{rule}; il pacchetto \textit{event} contiene gli eventi di notifica (BuildEvent) scambiati tra i due.`
  (removed the stray `.`; the domain has three sub-packages, as the domain package diagram shows)

### 9. §Model — record list
Added the missing `\item \texttt{Cooler}`. There are 9 records (see `permits` in the listing
right above); the list had 8.

### 10. §Model — PcBuilder inner classes
- `contiene al suo interno due classi astratte` → `contiene al suo interno due classi interne private`
  (`ReadOnlyView` and `BuildSnapshot` are private concrete inner classes, not abstract)
- `\texttt{ReadonlyView}` → `\texttt{ReadOnlyView}` (actual class name)

### 11. §Specification — point 2
`\texttt{InvolvedComponents}` → `\texttt{involvedComponents()}` (actual method name)

### 12. §Creazione — "uniche non astratte"
- before: `... sono le \textbf{uniche non astratte}, quindi si possono creare istanze di specifiche \textbf{solo} secondo queste due tipologie e caratteristiche.`
- after: `... sono le \textbf{uniche sottoclassi concrete di} BuildSpecification, quindi si possono creare istanze di specifiche sulla build \textbf{solo} secondo queste due tipologie e caratteristiche (le classi concrete del pacchetto pattern.specification, come AndSpecification, vengono usate solo internamente dagli operatori logici).`
  (`AndSpecification`, `OrSpecification`, … are also non-abstract, so the old sentence was false)

### 13. §Creazione — factory method names
- before: `i metodi<z\textit{factory} \texttt{atMost}, \texttt{atLeast}, e \texttt{excacltyOne} (oltre a \texttt{multipleExcactlyOne} ...`
- after: `i metodi \textit{factory} \texttt{atMost}, \texttt{atLeast} e \texttt{exactly} (oltre a \texttt{mutipleExcatclyOne} ...`
  (the methods are called `exactly` and `mutipleExcatclyOne`, as in the listing right below; also removed the `<z` typo)

### 14. §Creazione — Specs listing
`public static BuildSpecification socketMatchSpecification` and `... gpuLengthFitsCaseSpecification`
→ `public static final BuildSpecification ...` (matches change 4)

### 15. §Logica — table "Esempi di regole ed eccezioni" (MISSING-COOLER example)
- id `13` → `12` (MISSING-COOLER is rule 12; 13 is MISSING-DISPLAY-OUTPUT)
- `Severity.ERROR` → `Severity.WARNING` (actual severity)
- added the missing `)`: `.or(Specs.atLeast(ComponentType.COOLING, 1),` → `.or(Specs.atLeast(ComponentType.COOLING, 1)),`

### 16. §Composizione con Guardie — rewritten rules listing (MISSING-COOLER)
- `Specs.excatcly(ComponentType.CPU, 1)` → `Specs.mutipleExcatclyOne(ComponentType.CPU)` (no
  `excatcly` method exists; this is what `DefaultRules` uses)
- `CPU {CPU} ships without...` → `"CPU {CPU} ships without..."` (missing opening quote)

### 17. §Observer
- `\texttt{RestoreMemento()}` → `\texttt{restoreMemento()}`
- `\texttt{InvertedIndexMap} di \texttt{BuildRegistry}` → `... di \texttt{RulesRegistry}` (no `BuildRegistry` class exists)
- call-flow `verbatim`: added the first step `-> history.save();  // 0. snapshot (Memento)`
  before `PcBuilder.addComponent(c)` (`PcBuildSession.add` saves first)

### 18. §Memento
`PcBuilder.BuildSnaphot` → `PcBuilder.BuildSnapshot`

### 19. §Strutture Dati
- `LinkedHasehdMap<>` → `LinkedHashMap<>`
- the `registry.lookup(...)` example was replaced. It used `ComponentType.COOLER` (which does
  not exist, the enum value is `COOLING`), passed two keys while the code always passes one, and
  listed only 3 rules. New version: `registry.lookup(Set.of(ComponentType.CPU))` with the
  **real** result (12 rules, taken from running the code against `DefaultRules`), plus one
  sentence: the 3 PSU power rules appear under every key because they depend on
  `EnumSet.allOf(ComponentType.class)`, so 12 rules are re-evaluated instead of 40.
