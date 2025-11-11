package com.example.expensetracker.mapper;

import com.example.expensetracker.dto.RecurringTransactionDto;
import com.example.expensetracker.dto.RecurringTransactionRequestDto;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.RecurringTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecurringTransactionMapper {
    
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    RecurringTransactionDto toDto(RecurringTransaction entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "nextExecutionDate", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "active", constant = "true")
    RecurringTransaction fromRequest(RecurringTransactionRequestDto dto, Category category);
}
