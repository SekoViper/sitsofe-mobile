package com.sitsofe.scanner.core.data

import com.sitsofe.scanner.core.db.CustomerEntity
import com.sitsofe.scanner.core.network.CustomerDto

fun CustomerDto.toEntity(): CustomerEntity =
    CustomerEntity(
        id = _id,
        name = name,
        phone = phone
    )

fun CustomerEntity.toDto(): CustomerDto =
    CustomerDto(
        _id = id,
        name = name,
        phone = phone
    )
