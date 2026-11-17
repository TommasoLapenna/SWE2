-- db/test_violations.sql  (SQLite, v3)
-- Ogni test tenta di violare un vincolo: il database DEVE rifiutarlo.
-- Eseguire DOPO test_data.sql. Ogni test e' in BEGIN ... ROLLBACK, quindi
-- non lascia dati sporchi anche se per errore venisse accettato.
--
-- Atteso: 24 errori (uno per test). Conteggio automatico:
--   sqlite3 prova.db < db/test_violations.sql 2>&1 | grep -cE "constraint failed|cannot store"
-- deve stampare 24. Se e' meno, un vincolo non funziona: guarda quale
-- ".print" non e' seguito da un errore.
PRAGMA foreign_keys = ON;

.print "1. componente con prezzo negativo"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'Test', 'PrezzoNegativo', -1);
ROLLBACK;

.print "2. componente con tipo inesistente (TABLET)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('TABLET', 'Test', 'TipoKO', 1);
ROLLBACK;

.print "3. cpu con threads < cores"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'Test', 'ThreadKO', 1);
INSERT INTO cpu (id_component, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'ThreadKO'), 'AM5', 8, 4, 3.0, 65, 0, 0);
ROLLBACK;

.print "4. cpu con socket inesistente (AM3)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'Test', 'SocketKO', 1);
INSERT INTO cpu (id_component, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'SocketKO'), 'AM3', 4, 8, 3.0, 65, 0, 0);
ROLLBACK;

.print "5. scheda madre con chipset inesistente (Z999)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('MOTHERBOARD', 'Test', 'ChipsetKO', 1);
INSERT INTO motherboard (id_component, socket, chipset, form_factor, memory_gen, ram_slots,
                         max_memory_gb, max_memory_speed_mhz, pcie_x16_slots, m2_slots, sata_ports)
VALUES ((SELECT id_component FROM component WHERE model = 'ChipsetKO'), 'AM5', 'Z999', 'ATX', 'DDR5', 4, 128, 6000, 1, 2, 4);
ROLLBACK;

.print "6. storage con tipo SDD (refuso di SSD)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('STORAGE', 'Test', 'TypoSDD', 1);
INSERT INTO storage (id_component, storage_type, storage_tech, capacity_gb)
VALUES ((SELECT id_component FROM component WHERE model = 'TypoSDD'), 'SDD', 'SATA', 500);
ROLLBACK;

.print "7. HDD con tecnologia M2_NVME"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('STORAGE', 'Test', 'HddNvme', 1);
INSERT INTO storage (id_component, storage_type, storage_tech, capacity_gb)
VALUES ((SELECT id_component FROM component WHERE model = 'HddNvme'), 'HDD', 'M2_NVME', 500);
ROLLBACK;

.print "8. cooler AIR con radiator_size_mm valorizzato"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Test', 'AirKO', 1);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'AirKO'), 'AIR', 150, 150, 240);
ROLLBACK;

.print "9. cooler LIQUID con height_mm valorizzato"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Test', 'LiquidKO', 1);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'LiquidKO'), 'LIQUID', 200, 100, 240);
ROLLBACK;

.print "10. cooler AIR senza nessuna dimensione (NULL e NULL: il caso che un CHECK ingenuo lascia passare)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Test', 'AirNull', 1);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'AirNull'), 'AIR', 150, NULL, NULL);
ROLLBACK;

.print "11. testo in una colonna numerica (tabelle STRICT)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'Test', 'TestoNumerico', 1);
INSERT INTO cpu (id_component, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'TestoNumerico'), 'AM5', 'otto', 16, 3.0, 65, 0, 0);
ROLLBACK;

.print "12. riga di cpu agganciata a un componente di tipo GPU (FK sulla coppia id, type)"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('GPU', 'Test', 'NonUnaCpu', 1);
INSERT INTO cpu (id_component, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'NonUnaCpu'), 'AM5', 8, 16, 3.0, 65, 0, 0);
ROLLBACK;

.print "13. riga di cpu che dichiara un altro tipo (CHECK type = 'CPU')"
BEGIN;
INSERT INTO component (type, brand, model, price) VALUES ('GPU', 'Test', 'TipoFalso', 1);
INSERT INTO cpu (id_component, type, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'TipoFalso'), 'GPU', 'AM5', 8, 16, 3.0, 65, 0, 0);
ROLLBACK;

.print "14. generazione di memoria inesistente tra quelle supportate dalla CPU (DDR3)"
BEGIN;
INSERT INTO component_support (id_component, type, attribute, value)
VALUES ((SELECT min(id_component) FROM cpu), 'CPU', 'MEMORY_GEN', 'DDR3');
ROLLBACK;

.print "15. attributo che non appartiene al tipo (SOCKET su una CPU)"
BEGIN;
INSERT INTO component_support (id_component, type, attribute, value)
VALUES ((SELECT min(id_component) FROM cpu), 'CPU', 'SOCKET', 'AM5');
ROLLBACK;

.print "16. attributo di un altro tipo dichiarando il tipo sbagliato (SOCKET 'COOLING' su una CPU: FK sulla coppia)"
BEGIN;
INSERT INTO component_support (id_component, type, attribute, value)
VALUES ((SELECT min(id_component) FROM cpu), 'COOLING', 'SOCKET', 'AM5');
ROLLBACK;

.print "17. radiatore non scritto in forma canonica ('0240')"
BEGIN;
INSERT INTO component_support (id_component, type, attribute, value)
VALUES ((SELECT min(id_component) FROM pc_case), 'CASE', 'RADIATOR_MM', '0240');
ROLLBACK;

.print "18. radiatore non numerico ('grande')"
BEGIN;
INSERT INTO component_support (id_component, type, attribute, value)
VALUES ((SELECT min(id_component) FROM pc_case), 'CASE', 'RADIATOR_MM', 'grande');
ROLLBACK;

.print "19. stesso valore due volte nello stesso insieme (PK)"
BEGIN;
INSERT INTO component_support (id_component, type, attribute, value)
SELECT id_component, type, attribute, value FROM component_support LIMIT 1;
ROLLBACK;

.print "20. build con nome vuoto"
BEGIN;
INSERT INTO build (name) VALUES ('   ');
ROLLBACK;

.print "21. build_component verso un componente inesistente"
BEGIN;
INSERT INTO build_component (id_build, id_component)
VALUES ((SELECT min(id_build) FROM build), 999999);
ROLLBACK;

.print "22. build_component con quantity = 0"
BEGIN;
INSERT INTO build_component (id_build, id_component, quantity)
VALUES ((SELECT min(id_build) FROM build),
        (SELECT id_component FROM component WHERE model = 'Liquid Freezer III 280'), 0);
ROLLBACK;

.print "23. cancellare un componente usato in una build (ON DELETE RESTRICT)"
BEGIN;
DELETE FROM component WHERE id_component = (SELECT id_component FROM build_component LIMIT 1);
ROLLBACK;

.print "24. stesso componente due volte nella stessa build (PK composta: si usa quantity)"
BEGIN;
INSERT INTO build_component (id_build, id_component)
SELECT id_build, id_component FROM build_component LIMIT 1;
ROLLBACK;

.print "--- dati rimasti dopo i test (atteso: 11 componenti, 1 build: come prima)"
SELECT (SELECT count(*) FROM component), (SELECT count(*) FROM build);
