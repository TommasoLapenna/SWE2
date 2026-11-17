-- db/schema.sql  (SQLite, v3: 13 tabelle)
--
--   component            padre della generalizzazione: id, tipo e colonne comuni
--   cpu, gpu, memory, storage, motherboard, power_supply, pc_case, cooler, peripheral
--                        una figlia per tipo, con le sole colonne specifiche
--   component_support    attributi multivalore (cosa supporta un componente)
--   build                build salvate
--   build_component      relazione N:M build - componente
--
-- Gli enum Java non hanno tabelle di lookup: i valori ammessi sono nei CHECK
-- "colonna IN (...)". Devono coincidere ESATTAMENTE con i nomi (name()) degli
-- enum Java: lo controlla il test db.EnumConstraintsTest.
PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------- COMPONENTE (padre)
-- L'id e' unico tra TUTTI i tipi, cosi' una build punta ai componenti con una sola tabella.
-- UNIQUE (id_component, type) serve alle figlie: la loro chiave esterna e' sulla coppia,
-- quindi una riga di cpu puo' agganciarsi solo a un componente di tipo 'CPU'.

CREATE TABLE component (
  id_component INTEGER PRIMARY KEY AUTOINCREMENT,
  type         TEXT NOT NULL,
  brand        TEXT NOT NULL,
  model        TEXT NOT NULL,
  price        REAL NOT NULL,
  CONSTRAINT ck_component_type  CHECK (type IN ('CPU', 'GPU', 'RAM', 'STORAGE', 'MOTHERBOARD', 'CASE',
                                                'POWER_SUPPLY', 'COOLING', 'PERIPHERAL')),
  CONSTRAINT ck_component_price CHECK (price >= 0),
  CONSTRAINT uq_component_type  UNIQUE (id_component, type)
) STRICT;

-- ---------------------------------------------------------------- FIGLIE (una per tipo)
-- Cancellando il componente sparisce anche la riga della figlia (ON DELETE CASCADE).
-- Che ogni componente abbia la sua riga nella figlia lo garantisce il DAO (una transazione).

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
  CONSTRAINT ck_cpu_socket  CHECK (socket IN ('AM4', 'AM5', 'LGA1200', 'LGA1700', 'LGA1851')),
  CONSTRAINT ck_cpu_cores   CHECK (cores > 0),
  CONSTRAINT ck_cpu_threads CHECK (threads >= cores),
  CONSTRAINT ck_cpu_clock   CHECK (base_clock_ghz > 0),
  CONSTRAINT ck_cpu_tdp     CHECK (tdp_watts > 0),
  CONSTRAINT ck_cpu_igpu    CHECK (integrated_gpu IN (0, 1)),
  CONSTRAINT ck_cpu_cooler  CHECK (cooler_included IN (0, 1)),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
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
  CONSTRAINT ck_memory_gen      CHECK (memory_gen IN ('DDR4', 'DDR5')),
  CONSTRAINT ck_memory_sticks   CHECK (sticks_qty > 0),
  CONSTRAINT ck_memory_capacity CHECK (stick_capacity_gb > 0),
  CONSTRAINT ck_memory_freq     CHECK (frequency_mhz > 0),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
) STRICT;

CREATE TABLE storage (
  id_component INTEGER PRIMARY KEY,
  type         TEXT NOT NULL DEFAULT 'STORAGE',
  storage_type TEXT NOT NULL,
  storage_tech TEXT NOT NULL,
  capacity_gb  INTEGER NOT NULL,
  CONSTRAINT ck_storage_type     CHECK (type = 'STORAGE'),
  CONSTRAINT ck_storage_kind     CHECK (storage_type IN ('HDD', 'SSD')),
  CONSTRAINT ck_storage_tech     CHECK (storage_tech IN ('SATA', 'M2_SATA', 'M2_NVME')),
  CONSTRAINT ck_storage_capacity CHECK (capacity_gb > 0),
  CONSTRAINT ck_storage_hdd_sata CHECK (storage_type <> 'HDD' OR storage_tech = 'SATA'),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
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
  CONSTRAINT ck_mobo_type        CHECK (type = 'MOTHERBOARD'),
  CONSTRAINT ck_mobo_socket      CHECK (socket IN ('AM4', 'AM5', 'LGA1200', 'LGA1700', 'LGA1851')),
  CONSTRAINT ck_mobo_chipset     CHECK (chipset IN ('X870E', 'X870', 'B850', 'X670', 'X670E', 'A620',
                                                    'Z890', 'B860', 'H810', 'Z790', 'Z690', 'B760',
                                                    'B660', 'H710', 'H610')),
  CONSTRAINT ck_mobo_form_factor CHECK (form_factor IN ('MINI_ITX', 'MICRO_ATX', 'ATX', 'E_ATX')),
  CONSTRAINT ck_mobo_memory_gen  CHECK (memory_gen IN ('DDR4', 'DDR5')),
  CONSTRAINT ck_mobo_ram_slots   CHECK (ram_slots > 0),
  CONSTRAINT ck_mobo_max_mem     CHECK (max_memory_gb > 0),
  CONSTRAINT ck_mobo_max_speed   CHECK (max_memory_speed_mhz > 0),
  CONSTRAINT ck_mobo_pcie        CHECK (pcie_x16_slots >= 0),
  CONSTRAINT ck_mobo_m2          CHECK (m2_slots >= 0),
  CONSTRAINT ck_mobo_sata        CHECK (sata_ports >= 0),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
) STRICT;

CREATE TABLE power_supply (
  id_component      INTEGER PRIMARY KEY,
  type              TEXT NOT NULL DEFAULT 'POWER_SUPPLY',
  wattage           INTEGER NOT NULL,
  efficiency_rating TEXT NOT NULL,
  form_factor       TEXT NOT NULL,
  pcie_connectors   INTEGER NOT NULL,
  CONSTRAINT ck_psu_type        CHECK (type = 'POWER_SUPPLY'),
  CONSTRAINT ck_psu_wattage     CHECK (wattage > 0),
  CONSTRAINT ck_psu_efficiency  CHECK (efficiency_rating IN ('STANDARD', 'BRONZE', 'SILVER', 'GOLD',
                                                             'PLATINUM', 'TITANIUM')),
  CONSTRAINT ck_psu_form_factor CHECK (form_factor IN ('SFX', 'SFX_L', 'ATX')),
  CONSTRAINT ck_psu_pcie        CHECK (pcie_connectors >= 0),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
) STRICT;

-- form factor supportati (scheda madre e alimentatore) e radiatori: in component_support
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

-- socket supportati: in component_support
CREATE TABLE cooler (
  id_component     INTEGER PRIMARY KEY,
  type             TEXT NOT NULL DEFAULT 'COOLING',
  cooler_type      TEXT NOT NULL,
  max_tdp_watts    INTEGER NOT NULL,   -- TDP dissipabile, non consumo
  height_mm        INTEGER,            -- solo AIR
  radiator_size_mm INTEGER,            -- solo LIQUID
  CONSTRAINT ck_cooler_type    CHECK (type = 'COOLING'),
  CONSTRAINT ck_cooler_kind    CHECK (cooler_type IN ('AIR', 'LIQUID')),
  CONSTRAINT ck_cooler_max_tdp CHECK (max_tdp_watts > 0),

  -- un CHECK che vale NULL viene ACCETTATO, quindi serve "IS NOT NULL" esplicito sul campo obbligatorio.

  CONSTRAINT ck_cooler_dimensions CHECK (
    (cooler_type = 'AIR'    AND height_mm IS NOT NULL AND height_mm > 0
                            AND radiator_size_mm IS NULL)
    OR
    (cooler_type = 'LIQUID' AND radiator_size_mm IS NOT NULL AND radiator_size_mm > 0
                            AND height_mm IS NULL)
  ),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
) STRICT;

CREATE TABLE peripheral (
  id_component     INTEGER PRIMARY KEY,
  type             TEXT NOT NULL DEFAULT 'PERIPHERAL',
  power_draw_watts INTEGER NOT NULL,
  CONSTRAINT ck_peripheral_type  CHECK (type = 'PERIPHERAL'),
  CONSTRAINT ck_peripheral_power CHECK (power_draw_watts >= 0),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
) STRICT;

-- ---------------------------------------------------------------- ATTRIBUTI MULTIVALORE
-- Una riga per ogni valore di un insieme (Set) del componente:
--   CPU     MEMORY_GEN       generazioni di RAM supportate         (minimo 1)
--   CASE    FORM_FACTOR      form factor di scheda madre           (minimo 1)
--   CASE    PSU_FORM_FACTOR  form factor di alimentatore           (minimo 1)
--   CASE    RADIATOR_MM      radiatori montabili, in mm            (anche nessuno)
--   COOLING SOCKET           socket supportati                     (minimo 1)
-- Il CHECK ammette solo queste coppie tipo/attributo, ciascuna con i suoi valori.
-- Il "minimo 1" non si puo' esprimere nel database: lo garantisce il DAO.
-- value e' TEXT: per RADIATOR_MM deve essere un intero positivo scritto in forma canonica ('240').

CREATE TABLE component_support (
  id_component INTEGER NOT NULL,
  type         TEXT NOT NULL,
  attribute    TEXT NOT NULL,
  value        TEXT NOT NULL,
  PRIMARY KEY (id_component, attribute, value),
  CONSTRAINT ck_component_support CHECK (
    (type = 'CPU'     AND attribute = 'MEMORY_GEN'      AND value IN ('DDR4', 'DDR5'))
    OR
    (type = 'CASE'    AND attribute = 'FORM_FACTOR'     AND value IN ('MINI_ITX', 'MICRO_ATX', 'ATX', 'E_ATX'))
    OR
    (type = 'CASE'    AND attribute = 'PSU_FORM_FACTOR' AND value IN ('SFX', 'SFX_L', 'ATX'))
    OR
    (type = 'CASE'    AND attribute = 'RADIATOR_MM'     AND CAST(value AS INTEGER) > 0
                                                        AND value = CAST(CAST(value AS INTEGER) AS TEXT))
    OR
    (type = 'COOLING' AND attribute = 'SOCKET'          AND value IN ('AM4', 'AM5', 'LGA1200', 'LGA1700', 'LGA1851'))
  ),
  FOREIGN KEY (id_component, type) REFERENCES component (id_component, type) ON DELETE CASCADE
) STRICT;

-- ---------------------------------------------------------------- BUILD
-- Cancellando una build spariscono le sue righe; un componente usato in una
-- build NON si puo' cancellare (ON DELETE RESTRICT).
-- Lo stesso componente due volte nella build = una riga con quantity = 2.
-- Prezzo e consumo totali sono attributi derivati: si calcolano, non si salvano.

CREATE TABLE build (
  id_build   INTEGER PRIMARY KEY AUTOINCREMENT,
  name       TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- UTC, 'YYYY-MM-DD HH:MM:SS'
  CONSTRAINT ck_build_name CHECK (trim(name) <> '')
) STRICT;

CREATE TABLE build_component (
  id_build     INTEGER NOT NULL,
  id_component INTEGER NOT NULL,
  quantity     INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (id_build, id_component),
  CONSTRAINT ck_build_component_qty CHECK (quantity > 0),
  FOREIGN KEY (id_build)     REFERENCES build (id_build)         ON DELETE CASCADE,
  FOREIGN KEY (id_component) REFERENCES component (id_component) ON DELETE RESTRICT
) STRICT;

-- SQLite non indicizza da solo le colonne delle FK: serve per il controllo RESTRICT
CREATE INDEX idx_build_component_component ON build_component (id_component);
