-- db/test_data.sql  (SQLite, v3)
-- Dati di prova: prodotti reali ma specifiche e prezzi semplificati/inventati.
-- Eseguire DOPO schema.sql, su un database vuoto. Tutto deve andare a buon fine:
--   sqlite3 prova.db < db/schema.sql && sqlite3 prova.db < db/test_data.sql
-- Ogni componente e' una riga di component (padre) piu' una riga della tabella del suo tipo.
-- I modelli sono unici nei dati di prova, quindi li uso per ritrovare gli id.
PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------- CPU
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'AMD', 'Ryzen 7 7800X3D', 389.00);
INSERT INTO cpu (id_component, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'Ryzen 7 7800X3D'), 'AM5', 8, 16, 4.20, 120, 1, 0);
INSERT INTO component_support (id_component, type, attribute, value)
SELECT id_component, type, 'MEMORY_GEN', v FROM component, (SELECT 'DDR5' AS v)
WHERE model = 'Ryzen 7 7800X3D';

-- ---------------------------------------------------------------- GPU
INSERT INTO component (type, brand, model, price) VALUES ('GPU', 'NVIDIA', 'GeForce RTX 4070', 599.00);
INSERT INTO gpu (id_component, tdp_watts, vram_gb, length_mm, slot_width, recommended_psu_watts, power_connectors)
VALUES ((SELECT id_component FROM component WHERE model = 'GeForce RTX 4070'), 200, 12, 244, 2.0, 650, 1);

-- ---------------------------------------------------------------- MEMORY
INSERT INTO component (type, brand, model, price) VALUES ('RAM', 'Corsair', 'Vengeance DDR5 32GB', 119.00);
INSERT INTO memory (id_component, memory_gen, sticks_qty, stick_capacity_gb, frequency_mhz)
VALUES ((SELECT id_component FROM component WHERE model = 'Vengeance DDR5 32GB'), 'DDR5', 2, 16, 6000);

-- ---------------------------------------------------------------- STORAGE
INSERT INTO component (type, brand, model, price) VALUES ('STORAGE', 'Samsung', '980 PRO 1TB', 99.00);
INSERT INTO storage (id_component, storage_type, storage_tech, capacity_gb)
VALUES ((SELECT id_component FROM component WHERE model = '980 PRO 1TB'), 'SSD', 'M2_NVME', 1000);
INSERT INTO component (type, brand, model, price) VALUES ('STORAGE', 'Seagate', 'Barracuda 2TB', 55.00);
INSERT INTO storage (id_component, storage_type, storage_tech, capacity_gb)
VALUES ((SELECT id_component FROM component WHERE model = 'Barracuda 2TB'), 'HDD', 'SATA', 2000);

-- ---------------------------------------------------------------- MOTHERBOARD
INSERT INTO component (type, brand, model, price) VALUES ('MOTHERBOARD', 'MSI', 'MAG B850 TOMAHAWK', 219.00);
INSERT INTO motherboard (id_component, socket, chipset, form_factor, memory_gen, ram_slots,
                         max_memory_gb, max_memory_speed_mhz, pcie_x16_slots, m2_slots, sata_ports)
VALUES ((SELECT id_component FROM component WHERE model = 'MAG B850 TOMAHAWK'), 'AM5', 'B850', 'ATX', 'DDR5', 4, 192, 8000, 2, 3, 4);

-- ---------------------------------------------------------------- POWER SUPPLY
INSERT INTO component (type, brand, model, price) VALUES ('POWER_SUPPLY', 'Corsair', 'RM750e', 99.00);
INSERT INTO power_supply (id_component, wattage, efficiency_rating, form_factor, pcie_connectors)
VALUES ((SELECT id_component FROM component WHERE model = 'RM750e'), 750, 'GOLD', 'ATX', 4);

-- ---------------------------------------------------------------- CASE
INSERT INTO component (type, brand, model, price) VALUES ('CASE', 'NZXT', 'H5 Flow', 89.00);
INSERT INTO pc_case (id_component, power_draw_watts, max_gpu_length_mm, max_cooler_height_mm, expansion_slots, drive_bays)
VALUES ((SELECT id_component FROM component WHERE model = 'H5 Flow'), 5, 365, 165, 7, 2);
INSERT INTO component_support (id_component, type, attribute, value)
SELECT id_component, type, 'FORM_FACTOR', v FROM component, (SELECT 'ATX' AS v UNION ALL SELECT 'MICRO_ATX' UNION ALL SELECT 'MINI_ITX')
WHERE model = 'H5 Flow';
INSERT INTO component_support (id_component, type, attribute, value)
SELECT id_component, type, 'PSU_FORM_FACTOR', v FROM component, (SELECT 'ATX' AS v)
WHERE model = 'H5 Flow';
INSERT INTO component_support (id_component, type, attribute, value)
SELECT id_component, type, 'RADIATOR_MM', v FROM component, (SELECT '240' AS v UNION ALL SELECT '280' UNION ALL SELECT '360')
WHERE model = 'H5 Flow';

-- ---------------------------------------------------------------- COOLER
INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Noctua', 'NH-D15', 109.00);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'NH-D15'), 'AIR', 250, 165, NULL);
INSERT INTO component_support (id_component, type, attribute, value)
SELECT id_component, type, 'SOCKET', v FROM component, (SELECT 'AM5' AS v UNION ALL SELECT 'LGA1700')
WHERE model = 'NH-D15';

INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Arctic', 'Liquid Freezer III 280', 99.00);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'Liquid Freezer III 280'), 'LIQUID', 300, NULL, 280);
INSERT INTO component_support (id_component, type, attribute, value)
SELECT id_component, type, 'SOCKET', v FROM component, (SELECT 'AM5' AS v UNION ALL SELECT 'LGA1700')
WHERE model = 'Liquid Freezer III 280';

-- ---------------------------------------------------------------- PERIPHERAL
INSERT INTO component (type, brand, model, price) VALUES ('PERIPHERAL', 'Logitech', 'G502', 49.00);
INSERT INTO peripheral (id_component, power_draw_watts)
VALUES ((SELECT id_component FROM component WHERE model = 'G502'), 2);

-- ---------------------------------------------------------------- BUILD DI PROVA
-- Tutto tranne il cooler a liquido; l'SSD due volte (una riga con quantity = 2).
INSERT INTO build (name) VALUES ('Build di prova');

INSERT INTO build_component (id_build, id_component)
SELECT (SELECT id_build FROM build WHERE name = 'Build di prova'), id_component
FROM component WHERE model <> 'Liquid Freezer III 280';

UPDATE build_component SET quantity = 2
WHERE id_component = (SELECT id_component FROM component WHERE model = '980 PRO 1TB');

-- ---------------------------------------------------------------- VERIFICHE
.print "--- componenti per tipo (atteso: CASE 1, COOLING 2, CPU 1, GPU 1, MOTHERBOARD 1, PERIPHERAL 1, POWER_SUPPLY 1, RAM 1, STORAGE 2)"
SELECT type, count(*) FROM component GROUP BY type ORDER BY type;

.print "--- componenti senza la riga nella tabella del loro tipo (atteso: 0)"
SELECT count(*) FROM component c
WHERE NOT EXISTS (SELECT 1 FROM cpu          d WHERE d.id_component = c.id_component)
  AND NOT EXISTS (SELECT 1 FROM gpu          d WHERE d.id_component = c.id_component)
  AND NOT EXISTS (SELECT 1 FROM memory       d WHERE d.id_component = c.id_component)
  AND NOT EXISTS (SELECT 1 FROM storage      d WHERE d.id_component = c.id_component)
  AND NOT EXISTS (SELECT 1 FROM motherboard  d WHERE d.id_component = c.id_component)
  AND NOT EXISTS (SELECT 1 FROM power_supply d WHERE d.id_component = c.id_component)
  AND NOT EXISTS (SELECT 1 FROM pc_case      d WHERE d.id_component = c.id_component)
  AND NOT EXISTS (SELECT 1 FROM cooler       d WHERE d.id_component = c.id_component)
  AND NOT EXISTS (SELECT 1 FROM peripheral   d WHERE d.id_component = c.id_component);

.print "--- attributi multivalore (atteso: FORM_FACTOR 3, MEMORY_GEN 1, PSU_FORM_FACTOR 1, RADIATOR_MM 3, SOCKET 4)"
SELECT attribute, count(*) FROM component_support GROUP BY attribute ORDER BY attribute;

-- Questa e' la query del BuildDao: una sola SELECT, il tipo viene dal padre.
.print "--- contenuto della build (atteso: 10 righe, l'SSD con quantity 2)"
SELECT c.type, c.brand, c.model, bc.quantity
FROM build_component bc JOIN component c ON c.id_component = bc.id_component
WHERE bc.id_build = (SELECT id_build FROM build WHERE name = 'Build di prova')
ORDER BY bc.id_component;

.print "--- prezzo totale della build, derivato (atteso: 1925.0)"
SELECT sum(c.price * bc.quantity)
FROM build_component bc JOIN component c ON c.id_component = bc.id_component
WHERE bc.id_build = (SELECT id_build FROM build WHERE name = 'Build di prova');
