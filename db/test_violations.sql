-- db/test_violations.sql  (SQLite, v2)
-- Ogni test tenta di violare un vincolo: il database DEVE rifiutarlo.
-- Eseguire DOPO test_data.sql. Ogni test e' in BEGIN ... ROLLBACK, quindi
-- non lascia dati sporchi anche se per errore venisse accettato.
--
-- Atteso: 15 errori (uno per test). Conteggio automatico:
--   sqlite3 db/pcconfigurator.db < db/test_violations.sql 2>&1 | grep -cE "constraint failed|cannot store"
-- deve stampare 15. Se e' meno, un vincolo non funziona: guarda quale
-- ".print" non e' seguito da un errore.
PRAGMA foreign_keys = ON;

.print "1. cpu con prezzo negativo"
BEGIN;
INSERT INTO cpu (brand, model, price, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ('Test', 'PrezzoNegativo', -1, 'AM5', 8, 16, 3.0, 65, 0, 0);
ROLLBACK;

.print "2. cpu con threads < cores"
BEGIN;
INSERT INTO cpu (brand, model, price, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ('Test', 'ThreadKO', 1, 'AM5', 8, 4, 3.0, 65, 0, 0);
ROLLBACK;

.print "3. cpu con socket inesistente (AM3)"
BEGIN;
INSERT INTO cpu (brand, model, price, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ('Test', 'SocketKO', 1, 'AM3', 4, 8, 3.0, 65, 0, 0);
ROLLBACK;

.print "4. scheda madre con chipset inesistente (Z999)"
BEGIN;
INSERT INTO motherboard (brand, model, price, socket, chipset, form_factor, memory_gen, ram_slots,
                         max_memory_gb, max_memory_speed_mhz, pcie_x16_slots, m2_slots, sata_ports)
VALUES ('Test', 'ChipsetKO', 1, 'AM5', 'Z999', 'ATX', 'DDR5', 4, 128, 6000, 1, 2, 4);
ROLLBACK;

.print "5. storage con tipo SDD (refuso di SSD)"
BEGIN;
INSERT INTO storage (brand, model, price, storage_type, storage_tech, capacity_gb)
VALUES ('Test', 'TypoSDD', 1, 'SDD', 'SATA', 500);
ROLLBACK;

.print "6. HDD con tecnologia M2_NVME"
BEGIN;
INSERT INTO storage (brand, model, price, storage_type, storage_tech, capacity_gb)
VALUES ('Test', 'HddNvme', 1, 'HDD', 'M2_NVME', 500);
ROLLBACK;

.print "7. cooler AIR con radiator_size_mm valorizzato"
BEGIN;
INSERT INTO cooler (brand, model, price, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ('Test', 'AirKO', 1, 'AIR', 150, 150, 240);
ROLLBACK;

.print "8. cooler LIQUID con height_mm valorizzato"
BEGIN;
INSERT INTO cooler (brand, model, price, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ('Test', 'LiquidKO', 1, 'LIQUID', 200, 100, 240);
ROLLBACK;

.print "9. cooler AIR senza nessuna dimensione (NULL e NULL: il caso che un CHECK ingenuo lascia passare)"
BEGIN;
INSERT INTO cooler (brand, model, price, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ('Test', 'AirNull', 1, 'AIR', 150, NULL, NULL);
ROLLBACK;

.print "10. build_cpu verso una CPU inesistente"
BEGIN;
INSERT INTO build_cpu (id_build, id_cpu)
VALUES ((SELECT min(id_build) FROM build), 999999);
ROLLBACK;

.print "11. build_cpu con quantity = 0"
BEGIN;
INSERT INTO build_cpu (id_build, id_cpu, quantity)
VALUES ((SELECT min(id_build) FROM build), (SELECT min(id_cpu) FROM cpu), 0);
ROLLBACK;

.print "12. cancellare una CPU usata in una build (ON DELETE RESTRICT)"
BEGIN;
DELETE FROM cpu WHERE id_cpu = (SELECT id_cpu FROM build_cpu LIMIT 1);
ROLLBACK;

.print "13. stessa CPU due volte nella stessa build (PK composta)"
BEGIN;
INSERT INTO build_cpu (id_build, id_cpu)
SELECT id_build, id_cpu FROM build_cpu LIMIT 1;
ROLLBACK;

.print "14. testo in una colonna numerica (tabelle STRICT)"
BEGIN;
INSERT INTO cpu (brand, model, price, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ('Test', 'TestoNumerico', 1, 'AM5', 'otto', 16, 3.0, 65, 0, 0);
ROLLBACK;

.print "15. generazione di memoria inesistente nella tabella ponte della CPU (DDR3)"
BEGIN;
INSERT INTO cpu_memory_gen (id_cpu, memory_gen)
VALUES ((SELECT min(id_cpu) FROM cpu), 'DDR3');
ROLLBACK;
