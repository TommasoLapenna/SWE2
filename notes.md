```
pc-configurator/
├── pom.xml
└── src/
├── main/java/com/pcconfig/
│   │
│   ├── model/                        # Domain, mostly pattern-agnostic
│   │   ├── Component.java            # interface: type(), name(), price(), powerDraw(), supports()
│   │   ├── ComponentType.java        # enum: CPU, MOTHERBOARD, RAM, GPU, STORAGE, PSU, CASE, COOLER
│   │   ├── Attribute.java            # enum: SOCKET, MEMORY_TYPE, FORM_FACTOR, ...
│   │   ├── Cpu.java, Motherboard.java, Ram.java, Gpu.java, ... (implement Component)
│   │   ├── Slot.java                 # capacity-aware container per ComponentType
│   │   └── Computer.java             # EnumMap<ComponentType, Slot>, the Observer subject
│   │
│   ├── build/                        # Builder pattern
│   │   ├── ComputerBuilder.java
│   │   └── PresetDirector.java        # "Gaming PC", "Office PC" recipes
│   │
│   ├── catalog/                      # factory + Singleton
│   │   ├── ComponentFactory.java      # or per-type factories
│   │   ├── ComponentCatalog.java      # Singleton, holds all available parts
│   │   └── CatalogLoader.java         # Adapter, if loading from JSON/CSV
│   │
│   ├── compatibility/                 # Specification pattern
│   │   ├── CompatibilitySpec.java     # abstract: isSatisfiedBy, id, severity, describeFailure, involved
│   │   ├── AttributeMatchSpec.java    # generic socket/memory/form-factor check
│   │   ├── GpuClearanceSpec.java      # dimension checks
│   │   ├── PowerBudgetSpec.java       # uses PowerVisitor internally
│   │   ├── Severity.java
│   │   ├── CompatibilityIssue.java    # record: severity, rule id, message, involved components
│   │   ├── CompatibilityReport.java   # Collecting Parameter / Notification
│   │   └── CompatibilityChecker.java  # runs a List<CompatibilitySpec>, could hold named rule sets
│   │
│   ├── visitor/                       # Visitor pattern
│   │   ├── ComponentVisitor.java       # interface with visit(Cpu), visit(Ram), ...
│   │   ├── PriceVisitor.java
│   │   └── PowerDrawVisitor.java
│   │
│   ├── decorator/                     # Decorator pattern
│   │   ├── PricedItem.java            # common interface: price(), description()
│   │   ├── WarrantyDecorator.java
│   │   └── AssemblyServiceDecorator.java
│   │
│   ├── command/                       # Command pattern (undo/redo)
│   │   ├── ConfigCommand.java         # execute(), undo()
│   │   ├── AddComponentCommand.java
│   │   ├── RemoveComponentCommand.java
│   │   └── CommandHistory.java        # stack-based invoker
│   │
│   ├── observer/                      # Observer pattern
│   │   ├── ConfigurationObserver.java
│   │   ├── PriceDisplay.java
│   │   ├── PowerMeterDisplay.java
│   │   └── CompatibilityStatusPanel.java
│   │
│   ├── prototype/                     # Prototype pattern
│   │   └── ComputerPrototypeRegistry.java  # cloneable presets to customize
│   │
│   ├── service/                       # Facade pattern
│   │   └── PcConfiguratorService.java  # single entry point the UI/main talks to
│   │
│   └── Main.java                      # or a small CLI/GUI launcher
│
└── test/java/com/pcconfig/
├── compatibility/
│   ├── AttributeMatchSpecTest.java
│   ├── PowerBudgetSpecTest.java
│   └── CompatibilityCheckerTest.java
├── visitor/
│   ├── PriceVisitorTest.java
│   └── PowerDrawVisitorTest.java
├── build/
│   └── ComputerBuilderTest.java
└── command/
└── CommandHistoryTest.java
```