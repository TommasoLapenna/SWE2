-- db/test_data.sql  (SQLite)
-- Dati di prova: prodotti reali ma specifiche e prezzi semplificati/inventati.
-- Eseguire DOPO schema.sql e seed_lookups.sql. Tutto deve andare a buon fine.
-- Ogni componente = INSERT in component + INSERT nel sottotipo. L'id del
-- sottotipo si ricava dal modello (unico nei dati di prova).
PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------- CPU
INSERT INTO component (type, brand, model, price) VALUES ('CPU', 'AMD', 'Ryzen 7 7800X3D', 389.00);
INSERT INTO cpu (id_component, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ((SELECT id_component FROM component WHERE model = 'Ryzen 7 7800X3D'), 'AM5', 8, 16, 4.20, 120, 1, 0);
INSERT INTO cpu_memory_gen (id_component, memory_gen)
VALUES ((SELECT id_component FROM component WHERE model = 'Ryzen 7 7800X3D'), 'DDR5');

-- ---------------------------------------------------------------- GPU
INSERT INTO component (type, brand, model, price) VALUES ('GPU', 'NVIDIA', 'GeForce RTX 4070', 599.00);
INSERT INTO gpu (id_component, tdp_watts, vram_gb, length_mm, slot_width, recommended_psu_watts, power_connectors)
VALUES ((SELECT id_component FROM component WHERE model = 'GeForce RTX 4070'), 200, 12, 244, 2.0, 650, 1);

-- ---------------------------------------------------------------- RAM
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
VALUES ((SELECT id_component FROM component WHERE model = 'MAG B850 TOMAHAWK'),
        'AM5', 'B850', 'ATX', 'DDR5', 4, 192, 8000, 2, 3, 4);

-- ---------------------------------------------------------------- POWER SUPPLY
INSERT INTO component (type, brand, model, price) VALUES ('POWER_SUPPLY', 'Corsair', 'RM750e', 99.00);
INSERT INTO power_supply (id_component, wattage, efficiency_rating, form_factor, pcie_connectors)
VALUES ((SELECT id_component FROM component WHERE model = 'RM750e'), 750, 'GOLD', 'ATX', 4);

-- ---------------------------------------------------------------- CASE
INSERT INTO component (type, brand, model, price) VALUES ('CASE', 'NZXT', 'H5 Flow', 89.00);
INSERT INTO pc_case (id_component, power_draw_watts, max_gpu_length_mm, max_cooler_height_mm, expansion_slots, drive_bays)
VALUES ((SELECT id_component FROM component WHERE model = 'H5 Flow'), 5, 365, 165, 7, 2);

INSERT INTO case_form_factor (id_component, form_factor)
SELECT id_component, ff FROM component, (SELECT 'ATX' AS ff UNION ALL SELECT 'MICRO_ATX' UNION ALL SELECT 'MINI_ITX')
WHERE model = 'H5 Flow';

INSERT INTO case_psu_form_factor (id_component, psu_form_factor)
VALUES ((SELECT id_component FROM component WHERE model = 'H5 Flow'), 'ATX');

INSERT INTO case_radiator (id_component, radiator_mm)
SELECT id_component, r FROM component, (SELECT 240 AS r UNION ALL SELECT 280 UNION ALL SELECT 360)
WHERE model = 'H5 Flow';

-- ---------------------------------------------------------------- COOLING
INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Noctua', 'NH-D15', 109.00);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'NH-D15'), 'AIR', 250, 165, NULL);
INSERT INTO cooler_socket (id_component, socket)
SELECT id_component, s FROM component, (SELECT 'AM5' AS s UNION ALL SELECT 'LGA1700')
WHERE model = 'NH-D15';

INSERT INTO component (type, brand, model, price) VALUES ('COOLING', 'Arctic', 'Liquid Freezer III 280', 99.00);
INSERT INTO cooler (id_component, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ((SELECT id_component FROM component WHERE model = 'Liquid Freezer III 280'), 'LIQUID', 300, NULL, 280);
INSERT INTO cooler_socket (id_component, socket)
SELECT id_component, s FROM component, (SELECT 'AM5' AS s UNION ALL SELECT 'LGA1700')
WHERE model = 'Liquid Freezer III 280';

-- ---------------------------------------------------------------- PERIPHERAL
INSERT INTO component (type, brand, model, price) VALUES ('PERIPHERAL', 'Logitech', 'G502', 49.00);
INSERT INTO peripheral (id_component, power_draw_watts)
VALUES ((SELECT id_component FROM component WHERE model = 'G502'), 2);

-- ---------------------------------------------------------------- BUILD DI PROVA
INSERT INTO build (nome) VALUES ('Build di prova');
INSERT INTO build_item (id_build, id_component, quantity)
SELECT (SELECT id_build FROM build WHERE nome = 'Build di prova'), id_component, 1
FROM component
WHERE model IN ('Ryzen 7 7800X3D', 'GeForce RTX 4070', 'Vengeance DDR5 32GB',
                '980 PRO 1TB', 'MAG B850 TOMAHAWK', 'RM750e', 'H5 Flow', 'NH-D15');

-- ---------------------------------------------------------------- VERIFICHE
.print "--- componenti per tipo (atteso: 11 totali: COOLING 2, STORAGE 2, gli altri 1)"
SELECT type, count(*) AS n FROM component GROUP BY type ORDER BY type;

.print "--- elementi della build (atteso: 8 righe)"
SELECT c.type, c.brand, c.model
FROM build_item bi JOIN component c USING (id_component)
ORDER BY c.type;
