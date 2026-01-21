CREATE DATABASE PharmacyDB;
GO
USE PharmacyDB;
GO

CREATE TABLE Medicine (
    medicine_id VARCHAR(10) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    unit NVARCHAR(20) NOT NULL,
    price FLOAT CHECK (price > 0)
);

-- Import dữ liệu sạch
BULK INSERT Medicine
FROM 'E:\Project-LAB-github\lab211-project-group1\data\clean_medicine_data.csv'
WITH (
    FIRSTROW = 2,
    FIELDTERMINATOR = ',',
    ROWTERMINATOR = '\n',
    CODEPAGE = '65001'
);
