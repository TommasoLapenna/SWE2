PRAGMA foreign_keys = ON;

-- Tabelle di Lookup

CREATE TABLE socket            (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE memory_gen        (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE form_factor       (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE psu_form_factor   (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE storage_tech      (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE storage_type      (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE cooler_type       (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE efficiency_rating (name TEXT NOT NULL PRIMARY KEY) STRICT;
CREATE TABLE chipset           (name TEXT NOT NULL PRIMARY KEY) STRICT;

-- Tabelle per componenti concreti. Colonne comuni: id, brand, model, price.

CREATE TABLE cpu (
  id_cpu          INTEGER PRIMARY KEY AUTOINCREMENT,
  brand           TEXT NOT NULL,
  model           TEXT NOT NULL,
  price           REAL NOT NULL,
  socket          TEXT NOT NULL,
  cores           INTEGER NOT NULL,
  threads         INTEGER NOT NULL,
  base_clock_ghz  REAL NOT NULL,
  tdp_watts       INTEGER NOT NULL,
  integrated_gpu  INTEGER NOT NULL,   -- booleano: 0/1
  cooler_included INTEGER NOT NULL,   -- booleano: 0/1
  CONSTRAINT ck_cpu_price   CHECK (price >= 0),
  CONSTRAINT ck_cpu_cores   CHECK (cores > 0),
  CONSTRAINT ck_cpu_threads CHECK (threads >= cores),
  CONSTRAINT ck_cpu_clock   CHECK (base_clock_ghz > 0),
  CONSTRAINT ck_cpu_tdp     CHECK (tdp_watts > 0),
  CONSTRAINT ck_cpu_igpu    CHECK (integrated_gpu IN (0, 1)),
  CONSTRAINT ck_cpu_cooler  CHECK (cooler_included IN (0, 1)),
  FOREIGN KEY (socket) REFERENCES socket (name)
) STRICT;

CREATE TABLE gpu (
  id_gpu                INTEGER PRIMARY KEY AUTOINCREMENT,
  brand                 TEXT NOT NULL,
  model                 TEXT NOT NULL,
  price                 REAL NOT NULL,
  tdp_watts             INTEGER NOT NULL,
  vram_gb               INTEGER NOT NULL,
  length_mm             INTEGER NOT NULL,
  slot_width            REAL NOT NULL,
  recommended_psu_watts INTEGER NOT NULL,
  power_connectors      INTEGER NOT NULL,
  CONSTRAINT ck_gpu_price      CHECK (price >= 0),
  CONSTRAINT ck_gpu_tdp        CHECK (tdp_watts > 0),
  CONSTRAINT ck_gpu_vram       CHECK (vram_gb > 0),
  CONSTRAINT ck_gpu_length     CHECK (length_mm > 0),
  CONSTRAINT ck_gpu_slot_width CHECK (slot_width > 0),
  CONSTRAINT ck_gpu_rec_psu    CHECK (recommended_psu_watts > 0),
  CONSTRAINT ck_gpu_power_conn CHECK (power_connectors >= 0)  -- alcune GPU sono alimentate solo dallo slot
) STRICT;

CREATE TABLE memory (
  id_memory         INTEGER PRIMARY KEY AUTOINCREMENT,
  brand             TEXT NOT NULL,
  model             TEXT NOT NULL,
  price             REAL NOT NULL,
  memory_gen        TEXT NOT NULL,
  sticks_qty        INTEGER NOT NULL,   -- numero di moduli del kit
  stick_capacity_gb INTEGER NOT NULL,
  frequency_mhz     INTEGER NOT NULL,
  CONSTRAINT ck_memory_price    CHECK (price >= 0),
  CONSTRAINT ck_memory_sticks   CHECK (sticks_qty > 0),
  CONSTRAINT ck_memory_capacity CHECK (stick_capacity_gb > 0),
  CONSTRAINT ck_memory_freq     CHECK (frequency_mhz > 0),
  FOREIGN KEY (memory_gen) REFERENCES memory_gen (name)
) STRICT;

CREATE TABLE storage (
  id_storage   INTEGER PRIMARY KEY AUTOINCREMENT,
  brand        TEXT NOT NULL,
  model        TEXT NOT NULL,
  price        REAL NOT NULL,
  storage_type TEXT NOT NULL,
  storage_tech TEXT NOT NULL,
  capacity_gb  INTEGER NOT NULL,
  CONSTRAINT ck_storage_price    CHECK (price >= 0),
  CONSTRAINT ck_storage_capacity CHECK (capacity_gb > 0),
  CONSTRAINT ck_storage_hdd_sata CHECK (storage_type <> 'HDD' OR storage_tech = 'SATA'),  -- opzionale
  FOREIGN KEY (storage_type) REFERENCES storage_type (name),
  FOREIGN KEY (storage_tech) REFERENCES storage_tech (name)
) STRICT;

CREATE TABLE motherboard (
  id_motherboard       INTEGER PRIMARY KEY AUTOINCREMENT,
  brand                TEXT NOT NULL,
  model                TEXT NOT NULL,
  price                REAL NOT NULL,
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
  CONSTRAINT ck_mobo_price     CHECK (price >= 0),
  CONSTRAINT ck_mobo_ram_slots CHECK (ram_slots > 0),
  CONSTRAINT ck_mobo_max_mem   CHECK (max_memory_gb > 0),
  CONSTRAINT ck_mobo_max_speed CHECK (max_memory_speed_mhz > 0),
  CONSTRAINT ck_mobo_pcie      CHECK (pcie_x16_slots >= 0),
  CONSTRAINT ck_mobo_m2        CHECK (m2_slots >= 0),
  CONSTRAINT ck_mobo_sata      CHECK (sata_ports >= 0),
  FOREIGN KEY (socket)      REFERENCES socket (name),
  FOREIGN KEY (chipset)     REFERENCES chipset (name),
  FOREIGN KEY (form_factor) REFERENCES form_factor (name),
  FOREIGN KEY (memory_gen)  REFERENCES memory_gen (name)
) STRICT;

CREATE TABLE power_supply (
  id_power_supply   INTEGER PRIMARY KEY AUTOINCREMENT,
  brand             TEXT NOT NULL,
  model             TEXT NOT NULL,
  price             REAL NOT NULL,
  wattage           INTEGER NOT NULL,
  efficiency_rating TEXT NOT NULL,
  form_factor       TEXT NOT NULL,
  pcie_connectors   INTEGER NOT NULL,
  CONSTRAINT ck_psu_price   CHECK (price >= 0),
  CONSTRAINT ck_psu_wattage CHECK (wattage > 0),
  CONSTRAINT ck_psu_pcie    CHECK (pcie_connectors >= 0),
  FOREIGN KEY (efficiency_rating) REFERENCES efficiency_rating (name),
  FOREIGN KEY (form_factor) REFERENCES psu_form_factor (name)
) STRICT;

CREATE TABLE pc_case (
  id_pc_case           INTEGER PRIMARY KEY AUTOINCREMENT,
  brand                TEXT NOT NULL,
  model                TEXT NOT NULL,
  price                REAL NOT NULL,
  power_draw_watts     INTEGER NOT NULL,   -- letto dai dati (ventole/RGB)
  max_gpu_length_mm    INTEGER NOT NULL,
  max_cooler_height_mm INTEGER NOT NULL,
  expansion_slots      INTEGER NOT NULL,
  drive_bays           INTEGER NOT NULL,
  CONSTRAINT ck_case_price      CHECK (price >= 0),
  CONSTRAINT ck_case_power      CHECK (power_draw_watts >= 0),
  CONSTRAINT ck_case_gpu_len    CHECK (max_gpu_length_mm > 0),
  CONSTRAINT ck_case_cooler_h   CHECK (max_cooler_height_mm > 0),
  CONSTRAINT ck_case_exp_slots  CHECK (expansion_slots > 0),
  CONSTRAINT ck_case_drive_bays CHECK (drive_bays >= 0)
) STRICT;

-- TABELLE MULTIVALORE, risolvono le relazioni molti a molti per i componenti che supportano più standard.

-- 'cpu_memory_gen' permette a una singola CPU di supportare diverse generazioni di RAM.
-- attributo multivalore Set<MemoryGen> (almeno 1 valore: garantito dal DAO)

CREATE TABLE cpu_memory_gen (
                                id_cpu     INTEGER NOT NULL,
                                memory_gen TEXT NOT NULL,
                                PRIMARY KEY (id_cpu, memory_gen),
                                FOREIGN KEY (id_cpu) REFERENCES cpu (id_cpu) ON DELETE CASCADE,
                                FOREIGN KEY (memory_gen) REFERENCES memory_gen (name)
) STRICT;

-- attributi multivalore del case (minimo 1 per form factor e psu: garantito dal DAO)

CREATE TABLE case_form_factor (
  id_pc_case  INTEGER NOT NULL,
  form_factor TEXT NOT NULL,
  PRIMARY KEY (id_pc_case, form_factor),
  FOREIGN KEY (id_pc_case) REFERENCES pc_case (id_pc_case) ON DELETE CASCADE,
  FOREIGN KEY (form_factor) REFERENCES form_factor (name)
) STRICT;

CREATE TABLE case_psu_form_factor (
  id_pc_case      INTEGER NOT NULL,
  psu_form_factor TEXT NOT NULL,
  PRIMARY KEY (id_pc_case, psu_form_factor),
  FOREIGN KEY (id_pc_case) REFERENCES pc_case (id_pc_case) ON DELETE CASCADE,
  FOREIGN KEY (psu_form_factor) REFERENCES psu_form_factor (name)
) STRICT;

CREATE TABLE case_radiator (
  id_pc_case  INTEGER NOT NULL,
  radiator_mm INTEGER NOT NULL,
  PRIMARY KEY (id_pc_case, radiator_mm),
  CONSTRAINT ck_case_radiator_mm CHECK (radiator_mm > 0),
  FOREIGN KEY (id_pc_case) REFERENCES pc_case (id_pc_case) ON DELETE CASCADE
) STRICT;

CREATE TABLE cooler (
  id_cooler        INTEGER PRIMARY KEY AUTOINCREMENT,
  brand            TEXT NOT NULL,
  model            TEXT NOT NULL,
  price            REAL NOT NULL,
  cooler_type      TEXT NOT NULL,
  max_tdp_watts    INTEGER NOT NULL,   -- TDP dissipabile, non consumo
  height_mm        INTEGER,            -- solo AIR
  radiator_size_mm INTEGER,            -- solo LIQUID
  CONSTRAINT ck_cooler_price   CHECK (price >= 0),
  CONSTRAINT ck_cooler_max_tdp CHECK (max_tdp_watts > 0),

  -- un CHECK che vale NULL viene ACCETTATO, quindi serve "IS NOT NULL" esplicito sul campo obbligatorio.

  CONSTRAINT ck_cooler_dimensions CHECK (
    (cooler_type = 'AIR'    AND height_mm IS NOT NULL AND height_mm > 0
                            AND radiator_size_mm IS NULL)
    OR
    (cooler_type = 'LIQUID' AND radiator_size_mm IS NOT NULL AND radiator_size_mm > 0
                            AND height_mm IS NULL)
  ),
  FOREIGN KEY (cooler_type) REFERENCES cooler_type (name)
) STRICT;

CREATE TABLE cooler_socket (
  id_cooler INTEGER NOT NULL,
  socket    TEXT NOT NULL,
  PRIMARY KEY (id_cooler, socket),
  FOREIGN KEY (id_cooler) REFERENCES cooler (id_cooler) ON DELETE CASCADE,
  FOREIGN KEY (socket) REFERENCES socket (name)
) STRICT;

CREATE TABLE peripheral (
  id_peripheral    INTEGER PRIMARY KEY AUTOINCREMENT,
  brand            TEXT NOT NULL,
  model            TEXT NOT NULL,
  price            REAL NOT NULL,
  power_draw_watts INTEGER NOT NULL,
  CONSTRAINT ck_peripheral_price CHECK (price >= 0),
  CONSTRAINT ck_peripheral_power CHECK (power_draw_watts >= 0)
) STRICT;

-- (relazione N:M BUILD - COMPONENTE tradotta una volta per ogni figlia).
-- Cancellando una build spariscono le sue righe; un componente usato in una
-- build NON si puo' cancellare (DELETE RESTRICT).

CREATE TABLE build (
  id_build       INTEGER PRIMARY KEY AUTOINCREMENT,
  nome           TEXT NOT NULL,
  data_creazione TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP   --'YYYY-MM-DD HH:MM:SS'
) STRICT;

CREATE TABLE build_cpu (
  id_build INTEGER NOT NULL,
  id_cpu   INTEGER NOT NULL,
  quantity INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_cpu),
  CONSTRAINT ck_build_cpu_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build) REFERENCES build (id_build) ON DELETE CASCADE,
  FOREIGN KEY (id_cpu)   REFERENCES cpu (id_cpu)     ON DELETE RESTRICT
) STRICT;

CREATE TABLE build_gpu (
  id_build INTEGER NOT NULL,
  id_gpu   INTEGER NOT NULL,
  quantity INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_gpu),
  CONSTRAINT ck_build_gpu_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build) REFERENCES build (id_build) ON DELETE CASCADE,
  FOREIGN KEY (id_gpu)   REFERENCES gpu (id_gpu)     ON DELETE RESTRICT
) STRICT;

CREATE TABLE build_memory (
  id_build  INTEGER NOT NULL,
  id_memory INTEGER NOT NULL,
  quantity  INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_memory),
  CONSTRAINT ck_build_memory_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build)  REFERENCES build (id_build)   ON DELETE CASCADE,
  FOREIGN KEY (id_memory) REFERENCES memory (id_memory) ON DELETE RESTRICT
) STRICT;

CREATE TABLE build_storage (
  id_build   INTEGER NOT NULL,
  id_storage INTEGER NOT NULL,
  quantity   INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_storage),
  CONSTRAINT ck_build_storage_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build)   REFERENCES build (id_build)     ON DELETE CASCADE,
  FOREIGN KEY (id_storage) REFERENCES storage (id_storage) ON DELETE RESTRICT
) STRICT;

CREATE TABLE build_motherboard (
  id_build       INTEGER NOT NULL,
  id_motherboard INTEGER NOT NULL,
  quantity       INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_motherboard),
  CONSTRAINT ck_build_motherboard_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build)       REFERENCES build (id_build)             ON DELETE CASCADE,
  FOREIGN KEY (id_motherboard) REFERENCES motherboard (id_motherboard) ON DELETE RESTRICT
) STRICT;

CREATE TABLE build_power_supply (
  id_build        INTEGER NOT NULL,
  id_power_supply INTEGER NOT NULL,
  quantity        INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_power_supply),
  CONSTRAINT ck_build_power_supply_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build)        REFERENCES build (id_build)                 ON DELETE CASCADE,
  FOREIGN KEY (id_power_supply) REFERENCES power_supply (id_power_supply)   ON DELETE RESTRICT
) STRICT;

CREATE TABLE build_pc_case (
  id_build   INTEGER NOT NULL,
  id_pc_case INTEGER NOT NULL,
  quantity   INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_pc_case),
  CONSTRAINT ck_build_pc_case_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build)   REFERENCES build (id_build)     ON DELETE CASCADE,
  FOREIGN KEY (id_pc_case) REFERENCES pc_case (id_pc_case) ON DELETE RESTRICT
) STRICT;

CREATE TABLE build_cooler (
  id_build  INTEGER NOT NULL,
  id_cooler INTEGER NOT NULL,
  quantity  INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_cooler),
  CONSTRAINT ck_build_cooler_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build)  REFERENCES build (id_build)   ON DELETE CASCADE,
  FOREIGN KEY (id_cooler) REFERENCES cooler (id_cooler) ON DELETE RESTRICT
) STRICT;

CREATE TABLE build_peripheral (
  id_build      INTEGER NOT NULL,
  id_peripheral INTEGER NOT NULL,
  quantity      INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_peripheral),
  CONSTRAINT ck_build_peripheral_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build)      REFERENCES build (id_build)           ON DELETE CASCADE,
  FOREIGN KEY (id_peripheral) REFERENCES peripheral (id_peripheral) ON DELETE RESTRICT
) STRICT;

-- SQLite non indicizza da solo le colonne delle FK: servono per il controllo RESTRICT
CREATE INDEX idx_build_cpu_cpu                  ON build_cpu (id_cpu);
CREATE INDEX idx_build_gpu_gpu                  ON build_gpu (id_gpu);
CREATE INDEX idx_build_memory_memory            ON build_memory (id_memory);
CREATE INDEX idx_build_storage_storage          ON build_storage (id_storage);
CREATE INDEX idx_build_motherboard_motherboard  ON build_motherboard (id_motherboard);
CREATE INDEX idx_build_power_supply_psu         ON build_power_supply (id_power_supply);
CREATE INDEX idx_build_pc_case_case             ON build_pc_case (id_pc_case);
CREATE INDEX idx_build_cooler_cooler            ON build_cooler (id_cooler);
CREATE INDEX idx_build_peripheral_peripheral    ON build_peripheral (id_peripheral);
