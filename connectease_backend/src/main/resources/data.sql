-- Insert default categories
INSERT INTO Categories (cid, name) VALUES
('cat-01', 'Cleaning'),
('cat-02', 'Plumbing'),
('cat-03', 'Electrical'),
('cat-04', 'Carpentry'),
('cat-05', 'Painting'),
('cat-06', 'Home Repair');

-- Insert default images for categories
INSERT INTO Service_Images (image_id, url, is_primary) VALUES
('img-cleaning', 'https://picsum.photos/id/1018/400/300', true),
('img-plumbing', 'https://picsum.photos/id/1035/400/300', true),
('img-electrical', 'https://picsum.photos/id/1044/400/300', true),
('img-carpentry', 'https://picsum.photos/id/1050/400/300', true),
('img-painting', 'https://picsum.photos/id/1069/400/300', true),
('img-home-repair', 'https://picsum.photos/id/1074/400/300', true);