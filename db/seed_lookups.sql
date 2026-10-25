-- db/seed_lookups.sql  (v2: senza component_type, che non serve piu')
-- I valori devono coincidere ESATTAMENTE con i nomi (name()) degli enum Java.
-- Presuppone gli enum gia corretti: SDD -> SSD, M4 -> AM4 (da confermare con i compagni).
-- Se aggiungono un valore a un enum, aggiungere qui la riga corrispondente.
PRAGMA foreign_keys = ON;

INSERT INTO socket (name) VALUES
  ('AM4'), ('AM5'), ('LGA1200'), ('LGA1700'), ('LGA1851');

INSERT INTO memory_gen (name) VALUES ('DDR4'), ('DDR5');

INSERT INTO form_factor (name) VALUES
  ('MINI_ITX'), ('MICRO_ATX'), ('ATX'), ('E_ATX');

INSERT INTO psu_form_factor (name) VALUES ('SFX'), ('SFX_L'), ('ATX');

INSERT INTO storage_tech (name) VALUES ('SATA'), ('M2_SATA'), ('M2_NVME');

INSERT INTO storage_type (name) VALUES ('HDD'), ('SSD');

INSERT INTO cooler_type (name) VALUES ('AIR'), ('LIQUID');

INSERT INTO efficiency_rating (name) VALUES
  ('STANDARD'), ('BRONZE'), ('SILVER'), ('GOLD'), ('PLATINUM'), ('TITANIUM');

INSERT INTO chipset (name) VALUES
  ('X870E'), ('X870'), ('B850'), ('X670'), ('X670E'), ('A620'),
  ('Z890'), ('B860'), ('H810'), ('Z790'), ('Z690'), ('B760'),
  ('B660'), ('H710'), ('H610');
