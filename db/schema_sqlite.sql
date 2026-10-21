-- db/schema.sql  (SQLite)
-- Traduzione a mano di schema.dbml. Richiede SQLite >= 3.37 (tabelle STRICT).
--
-- IMPORTANTE: in SQLite le foreign key sono SPENTE di default.
-- Vanno attivate su OGNI connessione (qui sotto per lo script; in JDBC con
-- ?foreign_keys=on nell'URL oppure SQLiteConfig.enforceForeignKeys(true)).
PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------------
-- Lookup (una per enum Java; name = Enum.name())
-- ---------------------------------------------------------------------------
CREATE TABLE component_type    (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE socket            (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE memory_gen        (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE form_factor       (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE psu_form_factor   (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE storage_tech      (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE storage_type      (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE cooler_type       (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE efficiency_rating (name TEXT NOT NULL PRIMARY KEY) STRICT;  -- enum Java: EfficencyRating
CREATE TABLE chipset           (name TEXT NOT NULL PRIMARY KEY) STRICT;

-- ---------------------------------------------------------------------------
-- Supertipo
-- ---------------------------------------------------------------------------
CREATE TABLE component (
  id_component INTEGER PRIMARY KEY AUTOINCREMENT,
  type         TEXT NOT NULL,
  brand        TEXT NOT NULL,
  model        TEXT NOT NULL,
  price        REAL NOT NULL,
  CONSTRAINT ck_component_price CHECK (price >= 0),
  CONSTRAINT uq_component_id_type UNIQUE (id_component, type),  -- target della FK composta
  FOREIGN KEY (type) REFERENCES component_type (name)
) STRICT;

-- ---------------------------------------------------------------------------
-- Sottotipi: PK = id_component; FK composta (id_component, type) garantisce
-- che il tipo del sottotipo coincida con quello del supertipo.
-- ---------------------------------------------------------------------------
CREATE TABLE cpu (
  id_component    INTEGER PRIMARY KEY,
  type            TEXT NOT NULL DEFAULT 'CPU',
  socket          TEXT NOT NULL,
  cores           INTEGER NOT NULL,
  threads         INTEGER NOT NULL,
  base_clock_ghz  REAL NOT NULL,
  tdp_watts       INTEGER NOT NULL,
  integrated_gpu  INTEGER NOT NULL,   -- booleano: 0/1
  cooler_included INTEGER NOT NULL,   -- booleano: 0/1
  CONSTRAINT ck_cpu_type    CHECK (type = 'CPU'),
  CONSTRAINT ck_cpu_cores   CHECK (cores > 0),
  CONSTRAINT ck_cpu_threads CHECK (threads >= cores),
  CONSTRAINT ck_cpu_clock   CHECK (base_clock_ghz > 0),
  CONSTRAINT ck_cpu_tdp     CHECK (tdp_watts > 0),
  CONSTRAINT ck_cpu_igpu    CHECK (integrated_gpu IN (0, 1)),
  CONSTRAINT ck_cpu_cooler  CHECK (cooler_included IN (0, 1)),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE,
  FOREIGN KEY (socket) REFERENCES socket (name)
) STRICT;

CREATE TABLE cpu_memory_gen (
  id_component INTEGER NOT NULL,
  memory_gen   TEXT NOT NULL,
  PRIMARY KEY (id_component, memory_gen),
  FOREIGN KEY (id_component) REFERENCES cpu (id_component) ON DELETE CASCADE,
  FOREIGN KEY (memory_gen) REFERENCES memory_gen (name)
) STRICT;

CREATE TABLE gpu (
  id_component          INTEGER PRIMARY KEY,
  type                  TEXT NOT NULL DEFAULT 'GPU',
  tdp_watts             INTEGER NOT NULL,
  vram_gb               INTEGER NOT NULL,
  length_mm             INTEGER NOT NULL,
  slot_width            REAL NOT NULL,
  recommended_psu_watts INTEGER NOT NULL,
  power_connectors      INTEGER NOT NULL,
  CONSTRAINT ck_gpu_type       CHECK (type = 'GPU'),
  CONSTRAINT ck_gpu_tdp        CHECK (tdp_watts > 0),
  CONSTRAINT ck_gpu_vram       CHECK (vram_gb > 0),
  CONSTRAINT ck_gpu_length     CHECK (length_mm > 0),
  CONSTRAINT ck_gpu_slot_width CHECK (slot_width > 0),
  CONSTRAINT ck_gpu_rec_psu    CHECK (recommended_psu_watts > 0),
  CONSTRAINT ck_gpu_power_conn CHECK (power_connectors >= 0),  -- alcune GPU sono alimentate solo dallo slot
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
) STRICT;

CREATE TABLE memory (
  id_component      INTEGER PRIMARY KEY,
  type              TEXT NOT NULL DEFAULT 'RAM',
  memory_gen        TEXT NOT NULL,
  sticks_qty        INTEGER NOT NULL,   -- numero di moduli del kit
  stick_capacity_gb INTEGER NOT NULL,
  frequency_mhz     INTEGER NOT NULL,
  CONSTRAINT ck_memory_type     CHECK (type = 'RAM'),
  CONSTRAINT ck_memory_sticks   CHECK (sticks_qty > 0),
  CONSTRAINT ck_memory_capacity CHECK (stick_capacity_gb > 0),
  CONSTRAINT ck_memory_freq     CHECK (frequency_mhz > 0),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE,
  FOREIGN KEY (memory_gen) REFERENCES memory_gen (name)
) STRICT;

CREATE TABLE storage (
  id_component INTEGER PRIMARY KEY,
  type         TEXT NOT NULL DEFAULT 'STORAGE',
  storage_type TEXT NOT NULL,
  storage_tech TEXT NOT NULL,
  capacity_gb  INTEGER NOT NULL,
  CONSTRAINT ck_storage_type     CHECK (type = 'STORAGE'),
  CONSTRAINT ck_storage_capacity CHECK (capacity_gb > 0),
  CONSTRAINT ck_storage_hdd_sata CHECK (storage_type <> 'HDD' OR storage_tech = 'SATA'),  -- opzionale
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE,
  FOREIGN KEY (storage_type) REFERENCES storage_type (name),
  FOREIGN KEY (storage_tech) REFERENCES storage_tech (name)
) STRICT;

CREATE TABLE motherboard (
  id_component         INTEGER PRIMARY KEY,
  type                 TEXT NOT NULL DEFAULT 'MOTHERBOARD',
  socket               TEXT NOT NULL,
  chipset              TEXT NOT NULL,
  form_factor          TEXT NOT NULL,
  memory_gen           TEXT NOT NULL,
  ram_slots            INTEGER NOT NULL,
  max_memory_gb        INTEGER NOT NULL,
  max_memory_speed_mhz INTEGER NOT NULL,
  pcie_x16_slots       INTEGER NOT NULL,
  m2_slots             INTEGER NOT NULL,
  sata_ports           INTEGER NOT NULL,
  CONSTRAINT ck_mobo_type      CHECK (type = 'MOTHERBOARD'),
  CONSTRAINT ck_mobo_ram_slots CHECK (ram_slots > 0),
  CONSTRAINT ck_mobo_max_mem   CHECK (max_memory_gb > 0),
  CONSTRAINT ck_mobo_max_speed CHECK (max_memory_speed_mhz > 0),
  CONSTRAINT ck_mobo_pcie      CHECK (pcie_x16_slots >= 0),
  CONSTRAINT ck_mobo_m2        CHECK (m2_slots >= 0),
  CONSTRAINT ck_mobo_sata      CHECK (sata_ports >= 0),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE,
  FOREIGN KEY (socket)      REFERENCES socket (name),
  FOREIGN KEY (chipset)     REFERENCES chipset (name),
  FOREIGN KEY (form_factor) REFERENCES form_factor (name),
  FOREIGN KEY (memory_gen)  REFERENCES memory_gen (name)
) STRICT;

CREATE TABLE power_supply (
  id_component      INTEGER PRIMARY KEY,
  type              TEXT NOT NULL DEFAULT 'POWER_SUPPLY',
  wattage           INTEGER NOT NULL,
  efficiency_rating TEXT NOT NULL,
  form_factor       TEXT NOT NULL,
  pcie_connectors   INTEGER NOT NULL,
  CONSTRAINT ck_psu_type    CHECK (type = 'POWER_SUPPLY'),
  CONSTRAINT ck_psu_wattage CHECK (wattage > 0),
  CONSTRAINT ck_psu_pcie    CHECK (pcie_connectors >= 0),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE,
  FOREIGN KEY (efficiency_rating) REFERENCES efficiency_rating (name),
  FOREIGN KEY (form_factor) REFERENCES psu_form_factor (name)
) STRICT;

CREATE TABLE pc_case (
  id_component         INTEGER PRIMARY KEY,
  type                 TEXT NOT NULL DEFAULT 'CASE',
  power_draw_watts     INTEGER NOT NULL,   -- letto dai dati (ventole/RGB)
  max_gpu_length_mm    INTEGER NOT NULL,
  max_cooler_height_mm INTEGER NOT NULL,
  expansion_slots      INTEGER NOT NULL,
  drive_bays           INTEGER NOT NULL,
  CONSTRAINT ck_case_type       CHECK (type = 'CASE'),
  CONSTRAINT ck_case_power      CHECK (power_draw_watts >= 0),
  CONSTRAINT ck_case_gpu_len    CHECK (max_gpu_length_mm > 0),
  CONSTRAINT ck_case_cooler_h   CHECK (max_cooler_height_mm > 0),
  CONSTRAINT ck_case_exp_slots  CHECK (expansion_slots > 0),
  CONSTRAINT ck_case_drive_bays CHECK (drive_bays >= 0),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
) STRICT;

CREATE TABLE case_form_factor (
  id_component INTEGER NOT NULL,
  form_factor  TEXT NOT NULL,
  PRIMARY KEY (id_component, form_factor),
  FOREIGN KEY (id_component) REFERENCES pc_case (id_component) ON DELETE CASCADE,
  FOREIGN KEY (form_factor) REFERENCES form_factor (name)
) STRICT;

CREATE TABLE case_psu_form_factor (
  id_component    INTEGER NOT NULL,
  psu_form_factor TEXT NOT NULL,
  PRIMARY KEY (id_component, psu_form_factor),
  FOREIGN KEY (id_component) REFERENCES pc_case (id_component) ON DELETE CASCADE,
  FOREIGN KEY (psu_form_factor) REFERENCES psu_form_factor (name)
) STRICT;

CREATE TABLE case_radiator (
  id_component INTEGER NOT NULL,
  radiator_mm  INTEGER NOT NULL,
  PRIMARY KEY (id_component, radiator_mm),
  CONSTRAINT ck_case_radiator_mm CHECK (radiator_mm > 0),
  FOREIGN KEY (id_component) REFERENCES pc_case (id_component) ON DELETE CASCADE
) STRICT;

CREATE TABLE cooler (
  id_component     INTEGER PRIMARY KEY,
  type             TEXT NOT NULL DEFAULT 'COOLING',
  cooler_type      TEXT NOT NULL,
  max_tdp_watts    INTEGER NOT NULL,   -- TDP dissipabile, non consumo
  height_mm        INTEGER,            -- solo AIR
  radiator_size_mm INTEGER,            -- solo LIQUID
  CONSTRAINT ck_cooler_type    CHECK (type = 'COOLING'),
  CONSTRAINT ck_cooler_max_tdp CHECK (max_tdp_watts > 0),
  -- Attenzione ai NULL: un CHECK che vale NULL viene ACCETTATO, quindi
  -- serve "IS NOT NULL" esplicito sul campo obbligatorio.
  CONSTRAINT ck_cooler_dimensions CHECK (
    (cooler_type = 'AIR'    AND height_mm IS NOT NULL AND height_mm > 0
                            AND radiator_size_mm IS NULL)
    OR
    (cooler_type = 'LIQUID' AND radiator_size_mm IS NOT NULL AND radiator_size_mm > 0
                            AND height_mm IS NULL)
  ),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE,
  FOREIGN KEY (cooler_type) REFERENCES cooler_type (name)
) STRICT;

CREATE TABLE cooler_socket (
  id_component INTEGER NOT NULL,
  socket       TEXT NOT NULL,
  PRIMARY KEY (id_component, socket),
  FOREIGN KEY (id_component) REFERENCES cooler (id_component) ON DELETE CASCADE,
  FOREIGN KEY (socket) REFERENCES socket (name)
) STRICT;

CREATE TABLE peripheral (
  id_component     INTEGER PRIMARY KEY,
  type             TEXT NOT NULL DEFAULT 'PERIPHERAL',
  power_draw_watts INTEGER NOT NULL,
  CONSTRAINT ck_peripheral_type  CHECK (type = 'PERIPHERAL'),
  CONSTRAINT ck_peripheral_power CHECK (power_draw_watts >= 0),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
) STRICT;

-- ---------------------------------------------------------------------------
-- Build salvate
-- ---------------------------------------------------------------------------
CREATE TABLE build (
  id_build       INTEGER PRIMARY KEY AUTOINCREMENT,
  nome           TEXT NOT NULL,
  data_creazione TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP   -- UTC, formato 'YYYY-MM-DD HH:MM:SS'
) STRICT;

CREATE TABLE build_item (
  id_build     INTEGER NOT NULL,
  id_component INTEGER NOT NULL,
  quantity     INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_component),
  CONSTRAINT ck_build_item_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build) REFERENCES build (id_build) ON DELETE CASCADE,
  FOREIGN KEY (id_component) REFERENCES component (id_component) ON DELETE RESTRICT
) STRICT;

-- SQLite non indicizza da solo le colonne delle FK: serve per il controllo RESTRICT
CREATE INDEX idx_build_item_component ON build_item (id_component);
