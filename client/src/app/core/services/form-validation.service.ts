import { Injectable, signal, computed } from '@angular/core';

export interface ValidationRule {
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  pattern?: RegExp;
  email?: boolean;
  custom?: (value: any) => string | null;
}

export interface ValidationConfig {
  [fieldName: string]: ValidationRule;
}

export interface ValidationResult {
  isValid: boolean;
  errors: { [fieldName: string]: string };
}

export interface FormState<T> {
  data: T;
  errors: { [key: string]: string };
  touched: { [key: string]: boolean };
  isValid: boolean;
  isDirty: boolean;
}

/**
 * Modern Form Validation Service using Angular Signals
 * Provides reusable form validation logic with reactive state management
 */
@Injectable({
  providedIn: 'root'
})
export class FormValidationService {

  /**
   * Create a reactive form state with validation
   */
  createFormState<T extends Record<string, any>>(
    initialData: T,
    validationConfig: ValidationConfig
  ) {
    // Form state signals
    const _data = signal<T>({ ...initialData });
    const _errors = signal<{ [key: string]: string }>({});
    const _touched = signal<{ [key: string]: boolean }>({});
    const _isDirty = signal<boolean>(false);

    // Computed signals
    const isValid = computed(() => Object.keys(_errors()).length === 0);
    const hasErrors = computed(() => !isValid());

    return {
      // Readonly signals
      data: _data.asReadonly(),
      errors: _errors.asReadonly(),
      touched: _touched.asReadonly(),
      isDirty: _isDirty.asReadonly(),
      isValid,
      hasErrors,

      // Methods
      updateField: (field: keyof T, value: any) => {
        _data.update(current => ({ ...current, [field]: value }));
        _isDirty.set(true);
        this.validateField(field as string, value, validationConfig, _errors, _touched);
      },

      touchField: (field: keyof T) => {
        _touched.update(current => ({ ...current, [field]: true }));
        const currentValue = _data()[field];
        this.validateField(field as string, currentValue, validationConfig, _errors, _touched);
      },

      validateAll: () => {
        const currentData = _data();
        const newErrors: { [key: string]: string } = {};
        const newTouched: { [key: string]: boolean } = {};

        Object.keys(validationConfig).forEach(field => {
          newTouched[field] = true;
          const error = this.validateSingleField(field, currentData[field], validationConfig[field]);
          if (error) {
            newErrors[field] = error;
          }
        });

        _errors.set(newErrors);
        _touched.set(newTouched);
        return Object.keys(newErrors).length === 0;
      },

      reset: () => {
        _data.set({ ...initialData });
        _errors.set({});
        _touched.set({});
        _isDirty.set(false);
      },

      setData: (newData: Partial<T>) => {
        _data.update(current => ({ ...current, ...newData }));
        _isDirty.set(true);
      },

      getFieldError: (field: keyof T) => _errors()[field as string] || null,
      hasFieldError: (field: keyof T) => !!_errors()[field as string],
      isFieldTouched: (field: keyof T) => !!_touched()[field as string]
    };
  }

  /**
   * Validate a single field
   */
  private validateField(
    field: string,
    value: any,
    config: ValidationConfig,
    errorsSignal: any,
    touchedSignal: any
  ): void {
    const rule = config[field];
    if (!rule) return;

    const error = this.validateSingleField(field, value, rule);

    errorsSignal.update((current: any) => {
      const updated = { ...current };
      if (error) {
        updated[field] = error;
      } else {
        delete updated[field];
      }
      return updated;
    });
  }

  /**
   * Validate a single field value against a rule
   */
  private validateSingleField(field: string, value: any, rule: ValidationRule): string | null {
    const stringValue = value?.toString().trim() || '';

    // Required validation
    if (rule.required && !stringValue) {
      return `${this.formatFieldName(field)} is required`;
    }

    // Skip other validations if field is empty and not required
    if (!stringValue && !rule.required) {
      return null;
    }

    // Min length validation
    if (rule.minLength && stringValue.length < rule.minLength) {
      return `${this.formatFieldName(field)} must be at least ${rule.minLength} characters`;
    }

    // Max length validation
    if (rule.maxLength && stringValue.length > rule.maxLength) {
      return `${this.formatFieldName(field)} must not exceed ${rule.maxLength} characters`;
    }

    // Pattern validation
    if (rule.pattern && !rule.pattern.test(stringValue)) {
      return `${this.formatFieldName(field)} format is invalid`;
    }

    // Email validation
    if (rule.email && !this.isValidEmail(stringValue)) {
      return `${this.formatFieldName(field)} must be a valid email address`;
    }

    // Custom validation
    if (rule.custom) {
      return rule.custom(value);
    }

    return null;
  }

  /**
   * Format field name for display
   */
  private formatFieldName(field: string): string {
    return field
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }

  /**
   * Email validation regex
   */
  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  /**
   * Common validation rules
   */
  static readonly COMMON_RULES = {
    required: { required: true },
    email: { required: true, email: true },
    code: { required: true, minLength: 2, maxLength: 50, pattern: /^[A-Z0-9_-]+$/i },
    name: { required: true, minLength: 2, maxLength: 100 },
    description: { maxLength: 500 },
    address: { required: true, maxLength: 200 },
    city: { required: true, maxLength: 100 },
    country: { required: true, maxLength: 100 },
    postalCode: { maxLength: 20, pattern: /^[A-Z0-9\s-]+$/i },
    phone: { pattern: /^[\+]?[1-9][\d]{0,15}$/ }
  };

  /**
   * Warehouse-specific validation config
   */
  static readonly WAREHOUSE_VALIDATION: ValidationConfig = {
    code: FormValidationService.COMMON_RULES.code,
    name: FormValidationService.COMMON_RULES.name,
    description: FormValidationService.COMMON_RULES.description,
    address: FormValidationService.COMMON_RULES.address,
    city: FormValidationService.COMMON_RULES.city,
    stateProvince: { maxLength: 100 },
    postalCode: FormValidationService.COMMON_RULES.postalCode,
    country: FormValidationService.COMMON_RULES.country
  };
}
