package com.rohan.cloudProject.model.validator;

import com.rohan.cloudProject.model.PayStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to create a custom Validator Enum Annotation.
 */
public class EnumPayStatusValidator implements ConstraintValidator<EnumValue, PayStatus> {
    private List<String> acceptedValues;

    private PayStatus[] subset;

    @Override
    public void initialize(EnumValue annotation) {
        subset = annotation.anyOf();
    }

    @Override
    public boolean isValid(PayStatus value, ConstraintValidatorContext context) {
        return value == null || Arrays.asList(subset)
                .contains(value);
    }

}
