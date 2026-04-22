-- Check vendor user
SELECT uid, email, full_name, role, created_at FROM Users WHERE email = 'sadiyatehreen@mail.com';

-- Check all vendors
SELECT uid, email, full_name, role FROM Users WHERE role = 'vendor';

-- Check all services
SELECT sid, vendor_id, name, cid, created_at FROM services;

-- Check services for this vendor (if exists)
SELECT s.sid, s.vendor_id, s.name, s.cid, s.price, s.total_views FROM services s 
WHERE s.vendor_id = (SELECT uid FROM Users WHERE email = 'sadiyatehreen@mail.com' LIMIT 1);

-- Check categories
SELECT cid, name FROM Categories;

-- Check service images
SELECT image_id, sid, url, is_primary FROM Service_Images;