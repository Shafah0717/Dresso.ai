package com.dresso.app.data.repository

import com.dresso.app.data.model.Brand
import com.dresso.app.data.model.Garment

interface BrandRepository {
    suspend fun getBrands(): List<Brand>

    suspend fun getBrand(brandId: String): Brand?

    suspend fun getGarments(brandId: String): List<Garment>

    suspend fun getGarment(garmentId: String): Garment?
}
