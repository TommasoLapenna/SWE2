-- db/test_violations.sql  (SQLite)
-- Ogni test tenta di violare un vincolo: il database DEVE rifiutarlo.
-- Eseguire DOPO test_data.sql. Ogni test e' in BEGIN ... ROLLBACK, quindi
-- non lascia dati sporchi.
--
-- Atteso: 15 errori (uno per test). Conteggio automatico:
--   sqlite3 pcconfigurator.db < db/test_violations.sql 2>&1 | grep -cE "constraint failed|cannot store"
-- deve stampare 15. Se e' meno, un vincolo non funziona: guarda quale
-- ".print" non e' seguito da un errore.
PRAGMA foreign_keys = ON;

.print "1. cpu collegata a un component di tipo GPU (FK composta)"
BEGIN;
INSERT INTO cpu (id_component, type, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
SELECT id_component, 'CPU', 'AM5', 8, 16, 4.0, 100, 0, 0 FROM component WHERE type = 'GPU' LIMIT 1;
ROLLBACK;

.print "2. component con type MEMORY (nell'enum il valore e' RAM)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('MEMORY', 'Test', 'TipoSbagliato', 1);
ROLLBACK;

.print "3. prezzo negativo"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'Test', 'PrezzoNegativo', -1);
ROLLBACK;

.print "4. cpu con threads < cores"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'Test', 'ThreadKO', 1);
INSERT INTO cpu (id_component, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'ThreadKO'), 'AM5', 8, 4, 3.0, 65, 0, 0);
ROLLBACK;

.print "5. cpu con socket inesistente (AM3)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'Test', 'SocketKO', 1);
INSERT INTO cpu (id_component, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'SocketKO'), 'AM3', 4, 8, 3.0, 65, 0, 0);
ROLLBACK;

.print "6. scheda madre con chipset inesistente (Z999)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('MOTHERBOARD', 'Test', 'ChipsetKO', 1);
INSERT INTO motherboard (id_component, socket, chipset, form_factor, memory_gen, ram_slots,
                         max_memory_gb, max_memory_speed_mhz, pcie_x16_slots, m2_slots, sata_ports)
VALUES ((SELECT id_component FROM component WHERE model = 'ChipsetKO'),
        'AM5', 'Z999', 'ATX', 'DDR5', 4, 128, 6000, 1, 2, 4);
ROLLBACK;

.print "7. storage con tipo SDD (refuso di SSD)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('STORAGE', 'Test', 'TypoSDD', 1);
INSERT INTO storage (id_component, storage_type, storage_tech, capacity_gb)
VALUES ((SELECT id_component FROM component WHERE model = 'TypoSDD'), 'SDD', 'SATA', 500);
ROLLBACK;

.print "8. HDD con tecnologia M2_NVME"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('STORAGE', 'Test', 'HddNvme', 1);
INSERT INTO storage (id_component, storage_type, storage_tech, capacity_gb)
VALUES ((SELECT id_component FROM component WHERE model = 'HddNvme'), 'HDD', 'M2_NVME', 500);
ROLLBACK;

.print "9. cooler AIR con radiator_size_mm valorizzato"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Test', 'AirKO', 1);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'AirKO'), 'AIR', 150, 150, 240);
ROLLBACK;

.print "10. cooler LIQUID con height_mm valorizzato"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Test', 'LiquidKO', 1);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'LiquidKO'), 'LIQUID', 200, 100, 240);
ROLLBACK;

.print "11. cooler AIR senza nessuna dimensione (NULL e NULL: il caso che un CHECK ingenuo lascia passare)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Test', 'AirNull', 1);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'AirNull'), 'AIR', 150, NULL, NULL);
ROLLBACK;

.print "12. build_item verso un componente inesistente"
BEGIN;
INSERT INTO build_item (id_build, id_component)
VALUES ((SELECT min(id_build) FROM build), 999999);
ROLLBACK;

.print "13. build_item con quantity = 0"
BEGIN;
INSERT INTO build_item (id_build, id_component, quantity)
VALUES ((SELECT min(id_build) FROM build),
        (SELECT id_component FROM component WHERE model = 'G502'), 0);
ROLLBACK;

.print "14. cancellare un componente usato in una build (ON DELETE RESTRICT)"
BEGIN;
DELETE FROM component WHERE id_component = (SELECT id_component FROM build_item LIMIT 1);
ROLLBACK;

.print "15. testo in una colonna numerica (tabelle STRICT)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'Test', 'TestoNumerico', 1);
INSERT INTO cpu (id_component, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'TestoNumerico'), 'AM5', 'otto', 16, 3.0, 65, 0, 0);
ROLLBACK;
