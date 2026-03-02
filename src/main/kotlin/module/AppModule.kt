package org.delcom.module

import org.delcom.repositories.IPlantRepository
import org.delcom.repositories.PlantRepository
import org.delcom.repositories.ISpiceRepository
import org.delcom.repositories.SpiceRepository
import org.delcom.services.PlantService
import org.delcom.services.ProfileService
import org.delcom.services.SpiceService
import org.koin.dsl.module

val appModule = module {
    // Plant Repository
    single<IPlantRepository> {
        PlantRepository()
    }

    // Plant Service
    single {
        PlantService(get())
    }

    // Spice Repository
    single<ISpiceRepository> {
        SpiceRepository()
    }

    // Spice Service
    single {
        SpiceService(get())
    }

    // Profile Service
    single {
        ProfileService()
    }
}