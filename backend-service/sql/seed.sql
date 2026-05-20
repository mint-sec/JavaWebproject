INSERT INTO cargo_profiles (cargo_type, cargo_name, safe_temp_min, safe_temp_max, sensitivity_level, description)
VALUES
('VACCINE', '疫苗', 2.00, 8.00, 'HIGH', '疫苗冷链主场景');

INSERT INTO vehicles (vehicle_code, plate_number, cargo_type, cargo_name, safe_temp_min, safe_temp_max, status)
VALUES
('CC-VA-01', '京A-1024', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-02', '京A-1025', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-03', '京A-1026', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-04', '京A-1027', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT'),
('CC-VA-05', '京A-1028', 'VACCINE', '疫苗', 2.00, 8.00, 'IN_TRANSIT');
