-- db/test_data.sql  (SQLite, v2)
-- Dati di prova: prodotti reali ma specifiche e prezzi semplificati/inventati.
-- Eseguire DOPO schema.sql e seed_lookups.sql. Tutto deve andare a buon fine.
-- I modelli sono unici nei dati di prova, quindi li uso per ritrovare gli id.
PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------- CPU
INSERT INTO cpu (brand, model, price, socket, cores, threads, base_clock_ghz, tdp_watts, integrated_gpu, cooler_included)
VALUES ('AMD', 'Ryzen 7 7800X3D', 389.00, 'AM5', 8, 16, 4.20, 120, 1, 0);
INSERT INTO cpu_memory_gen (id_cpu, memory_gen)
VALUES ((SELECT id_cpu FROM cpu WHERE model = 'Ryzen 7 7800X3D'), 'DDR5');

-- ---------------------------------------------------------------- GPU
INSERT INTO gpu (brand, model, price, tdp_watts, vram_gb, length_mm, slot_width, recommended_psu_watts, power_connectors)
VALUES ('NVIDIA', 'GeForce RTX 4070', 599.00, 200, 12, 244, 2.0, 650, 1);

-- ---------------------------------------------------------------- MEMORY
INSERT INTO memory (brand, model, price, memory_gen, sticks_qty, stick_capacity_gb, frequency_mhz)
VALUES ('Corsair', 'Vengeance DDR5 32GB', 119.00, 'DDR5', 2, 16, 6000);

-- ---------------------------------------------------------------- STORAGE
INSERT INTO storage (brand, model, price, storage_type, storage_tech, capacity_gb)
VALUES ('Samsung', '980 PRO 1TB', 99.00, 'SSD', 'M2_NVME', 1000);
INSERT INTO storage (brand, model, price, storage_type, storage_tech, capacity_gb)
VALUES ('Seagate', 'Barracuda 2TB', 55.00, 'HDD', 'SATA', 2000);

-- ---------------------------------------------------------------- MOTHERBOARD
INSERT INTO motherboard (brand, model, price, socket, chipset, form_factor, memory_gen, ram_slots,
                         max_memory_gb, max_memory_speed_mhz, pcie_x16_slots, m2_slots, sata_ports)
VALUES ('MSI', 'MAG B850 TOMAHAWK', 219.00, 'AM5', 'B850', 'ATX', 'DDR5', 4, 192, 8000, 2, 3, 4);

-- ---------------------------------------------------------------- POWER SUPPLY
INSERT INTO power_supply (brand, model, price, wattage, efficiency_rating, form_factor, pcie_connectors)
VALUES ('Corsair', 'RM750e', 99.00, 750, 'GOLD', 'ATX', 4);

-- ---------------------------------------------------------------- CASE
INSERT INTO pc_case (brand, model, price, power_draw_watts, max_gpu_length_mm, max_cooler_height_mm, expansion_slots, drive_bays)
VALUES ('NZXT', 'H5 Flow', 89.00, 5, 365, 165, 7, 2);

INSERT INTO case_form_factor (id_pc_case, form_factor)
SELECT id_pc_case, ff FROM pc_case, (SELECT 'ATX' AS ff UNION ALL SELECT 'MICRO_ATX' UNION ALL SELECT 'MINI_ITX')
WHERE model = 'H5 Flow';

INSERT INTO case_psu_form_factor (id_pc_case, psu_form_factor)
VALUES ((SELECT id_pc_case FROM pc_case WHERE model = 'H5 Flow'), 'ATX');

INSERT INTO case_radiator (id_pc_case, radiator_mm)
SELECT id_pc_case, r FROM pc_case, (SELECT 240 AS r UNION ALL SELECT 280 UNION ALL SELECT 360)
WHERE model = 'H5 Flow';

-- ---------------------------------------------------------------- COOLER
INSERT INTO cooler (brand, model, price, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ('Noctua', 'NH-D15', 109.00, 'AIR', 250, 165, NULL);
INSERT INTO cooler_socket (id_cooler, socket)
SELECT id_cooler, s FROM cooler, (SELECT 'AM5' AS s UNION ALL SELECT 'LGA1700')
WHERE model = 'NH-D15';

INSERT INTO cooler (brand, model, price, cooler_type, max_tdp_watts, height_mm, radiator_size_mm)
VALUES ('Arctic', 'Liquid Freezer III 280', 99.00, 'LIQUID', 300, NULL, 280);
INSERT INTO cooler_socket (id_cooler, socket)
SELECT id_cooler, s FROM cooler, (SELECT 'AM5' AS s UNION ALL SELECT 'LGA1700')
WHERE model = 'Liquid Freezer III 280';

-- ---------------------------------------------------------------- PERIPHERAL
INSERT INTO peripheral (brand, model, price, power_draw_watts)
VALUES ('Logitech', 'G502', 49.00, 2);

-- ---------------------------------------------------------------- BUILD DI PROVA
INSERT INTO build (nome) VALUES ('Build di prova');

INSERT INTO build_cpu (id_build, id_cpu)
VALUES ((SELECT id_build FROM build WHERE nome = 'Build di prova'),
        (SELECT id_cpu FROM cpu WHERE model = 'Ryzen 7 7800X3D'));
INSERT INTO build_gpu (id_build, id_gpu)
VALUES ((SELECT id_build FROM build WHERE nome = 'Build di prova'),
        (SELECT id_gpu FROM gpu WHERE model = 'GeForce RTX 4070'));
INSERT INTO build_memory (id_build, id_memory)
VALUES ((SELECT id_build FROM build WHERE nome = 'Build di prova'),
        (SELECT id_memory FROM memory WHERE model = 'Vengeance DDR5 32GB'));
INSERT INTO build_storage (id_build, id_storage)
SELECT (SELECT id_build FROM build WHERE nome = 'Build di prova'), id_storage
FROM storage WHERE model IN ('980 PRO 1TB', 'Barracuda 2TB');
INSERT INTO build_motherboard (id_build, id_motherboard)
VALUES ((SELECT id_build FROM build WHERE nome = 'Build di prova'),
        (SELECT id_motherboard FROM motherboard WHERE model = 'MAG B850 TOMAHAWK'));
INSERT INTO build_power_supply (id_build, id_power_supply)
VALUES ((SELECT id_build FROM build WHERE nome = 'Build di prova'),
        (SELECT id_power_supply FROM power_supply WHERE model = 'RM750e'));
INSERT INTO build_pc_case (id_build, id_pc_case)
VALUES ((SELECT id_build FROM build WHERE nome = 'Build di prova'),
        (SELECT id_pc_case FROM pc_case WHERE model = 'H5 Flow'));
INSERT INTO build_cooler (id_build, id_cooler)
VALUES ((SELECT id_build FROM build WHERE nome = 'Build di prova'),
        (SELECT id_cooler FROM cooler WHERE model = 'NH-D15'));

-- ---------------------------------------------------------------- VERIFICHE
.print "--- righe per tabella (atteso: cpu 1, gpu 1, memory 1, storage 2, motherboard 1, power_supply 1, pc_case 1, cooler 2, peripheral 1)"
SELECT 'cpu' AS tabella, count(*) AS n FROM cpu
UNION ALL SELECT 'gpu', count(*) FROM gpu
UNION ALL SELECT 'memory', count(*) FROM memory
UNION ALL SELECT 'storage', count(*) FROM storage
UNION ALL SELECT 'motherboard', count(*) FROM motherboard
UNION ALL SELECT 'power_supply', count(*) FROM power_supply
UNION ALL SELECT 'pc_case', count(*) FROM pc_case
UNION ALL SELECT 'cooler', count(*) FROM cooler
UNION ALL SELECT 'peripheral', count(*) FROM peripheral;

-- Questa query e' anche il modello per il BuildDao: una SELECT per tipo.
.print "--- contenuto della build (atteso: 9 righe)"
SELECT 'CPU' AS tipo, c.brand, c.model, b.quantity
  FROM build_cpu b JOIN cpu c USING (id_cpu)
  WHERE b.id_build = (SELECT id_build FROM build WHERE nome = 'Build di prova')
UNION ALL
SELECT 'GPU', c.brand, c.model, b.quantity
  FROM build_gpu b JOIN gpu c USING (id_gpu)
  WHERE b.id_build = (SELECT id_build FROM build WHERE nome = 'Build di prova')
UNION ALL
SELECT 'MEMORY', c.brand, c.model, b.quantity
  FROM build_memory b JOIN memory c USING (id_memory)
  WHERE b.id_build = (SELECT id_build FROM build WHERE nome = 'Build di prova')
UNION ALL
SELECT 'STORAGE', c.brand, c.model, b.quantity
  FROM build_storage b JOIN storage c USING (id_storage)
  WHERE b.id_build = (SELECT id_build FROM build WHERE nome = 'Build di prova')
UNION ALL
SELECT 'MOTHERBOARD', c.brand, c.model, b.quantity
  FROM build_motherboard b JOIN motherboard c USING (id_motherboard)
  WHERE b.id_build = (SELECT id_build FROM build WHERE nome = 'Build di prova')
UNION ALL
SELECT 'POWER_SUPPLY', c.brand, c.model, b.quantity
  FROM build_power_supply b JOIN power_supply c USING (id_power_supply)
  WHERE b.id_build = (SELECT id_build FROM build WHERE nome = 'Build di prova')
UNION ALL
SELECT 'CASE', c.brand, c.model, b.quantity
  FROM build_pc_case b JOIN pc_case c USING (id_pc_case)
  WHERE b.id_build = (SELECT id_build FROM build WHERE nome = 'Build di prova')
UNION ALL
SELECT 'COOLER', c.brand, c.model, b.quantity
  FROM build_cooler b JOIN cooler c USING (id_cooler)
  WHERE b.id_build = (SELECT id_build FROM build WHERE nome = 'Build di prova')
UNION ALL
SELECT 'PERIPHERAL', c.brand, c.model, b.quantity
  FROM build_peripheral b JOIN peripheral c USING (id_peripheral)
  WHERE b.id_build = (SELECT id_build FROM build WHERE nome = 'Build di prova');
