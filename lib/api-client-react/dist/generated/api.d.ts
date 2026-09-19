import type { QueryKey, UseMutationOptions, UseMutationResult, UseQueryOptions, UseQueryResult } from '@tanstack/react-query';
import type { EligibilityInput, EligibilityResult, GetDecisionModel200, HealthStatus, RuleConfiguration, RuleConfigurationUpdate } from './api.schemas';
import { customFetch } from '../custom-fetch';
import type { ErrorType, BodyType } from '../custom-fetch';
type AwaitedInput<T> = PromiseLike<T> | T;
type Awaited<O> = O extends AwaitedInput<infer T> ? T : never;
type SecondParameter<T extends (...args: never) => unknown> = Parameters<T>[1];
export declare const getHealthCheckUrl: () => string;
export declare const healthCheck: (options?: Parameters<typeof customFetch>[1]) => Promise<HealthStatus>;
export declare const getHealthCheckQueryKey: () => readonly ["/api/healthz"];
export declare const getHealthCheckQueryOptions: <TData = Awaited<ReturnType<typeof healthCheck>>, TError = ErrorType<unknown>>(options?: {
    query?: UseQueryOptions<Awaited<ReturnType<typeof healthCheck>>, TError, TData>;
    request?: SecondParameter<typeof customFetch>;
}) => UseQueryOptions<Awaited<ReturnType<typeof healthCheck>>, TError, TData> & {
    queryKey: QueryKey;
};
export type HealthCheckQueryResult = NonNullable<Awaited<ReturnType<typeof healthCheck>>>;
export type HealthCheckQueryError = ErrorType<unknown>;
export declare function useHealthCheck<TData = Awaited<ReturnType<typeof healthCheck>>, TError = ErrorType<unknown>>(options?: {
    query?: UseQueryOptions<Awaited<ReturnType<typeof healthCheck>>, TError, TData>;
    request?: SecondParameter<typeof customFetch>;
}): UseQueryResult<TData, TError> & {
    queryKey: QueryKey;
};
export declare const getGetRuleConfigurationUrl: () => string;
export declare const getRuleConfiguration: (options?: Parameters<typeof customFetch>[1]) => Promise<RuleConfiguration>;
export declare const getGetRuleConfigurationQueryKey: () => readonly ["/api/rules/config"];
export declare const getGetRuleConfigurationQueryOptions: <TData = Awaited<ReturnType<typeof getRuleConfiguration>>, TError = ErrorType<unknown>>(options?: {
    query?: UseQueryOptions<Awaited<ReturnType<typeof getRuleConfiguration>>, TError, TData>;
    request?: SecondParameter<typeof customFetch>;
}) => UseQueryOptions<Awaited<ReturnType<typeof getRuleConfiguration>>, TError, TData> & {
    queryKey: QueryKey;
};
export type GetRuleConfigurationQueryResult = NonNullable<Awaited<ReturnType<typeof getRuleConfiguration>>>;
export type GetRuleConfigurationQueryError = ErrorType<unknown>;
export declare function useGetRuleConfiguration<TData = Awaited<ReturnType<typeof getRuleConfiguration>>, TError = ErrorType<unknown>>(options?: {
    query?: UseQueryOptions<Awaited<ReturnType<typeof getRuleConfiguration>>, TError, TData>;
    request?: SecondParameter<typeof customFetch>;
}): UseQueryResult<TData, TError> & {
    queryKey: QueryKey;
};
export declare const getUpdateRuleConfigurationUrl: () => string;
export declare const updateRuleConfiguration: (ruleConfigurationUpdate: RuleConfigurationUpdate, options?: Parameters<typeof customFetch>[1]) => Promise<RuleConfiguration>;
export declare const getUpdateRuleConfigurationMutationKey: () => readonly ["updateRuleConfiguration"];
export declare const getUpdateRuleConfigurationMutationOptions: <TError = ErrorType<unknown>, TContext = unknown>(options?: {
    mutation?: UseMutationOptions<Awaited<ReturnType<typeof updateRuleConfiguration>>, TError, UpdateRuleConfigurationMutationVariables, TContext>;
    request?: SecondParameter<typeof customFetch>;
}) => UseMutationOptions<Awaited<ReturnType<typeof updateRuleConfiguration>>, TError, UpdateRuleConfigurationMutationVariables, TContext>;
export type UpdateRuleConfigurationMutationResult = NonNullable<Awaited<ReturnType<typeof updateRuleConfiguration>>>;
export type UpdateRuleConfigurationMutationBody = BodyType<RuleConfigurationUpdate>;
export type UpdateRuleConfigurationMutationError = ErrorType<unknown>;
export type UpdateRuleConfigurationMutationVariables = {
    data: BodyType<RuleConfigurationUpdate>;
};
export declare const useUpdateRuleConfiguration: <TError = ErrorType<unknown>, TContext = unknown>(options?: {
    mutation?: UseMutationOptions<Awaited<ReturnType<typeof updateRuleConfiguration>>, TError, UpdateRuleConfigurationMutationVariables, TContext>;
    request?: SecondParameter<typeof customFetch>;
}) => UseMutationResult<Awaited<ReturnType<typeof updateRuleConfiguration>>, TError, UpdateRuleConfigurationMutationVariables, TContext>;
export declare const getEvaluateEligibilityUrl: () => string;
export declare const evaluateEligibility: (eligibilityInput: EligibilityInput, options?: Parameters<typeof customFetch>[1]) => Promise<EligibilityResult>;
export declare const getEvaluateEligibilityMutationKey: () => readonly ["evaluateEligibility"];
export declare const getEvaluateEligibilityMutationOptions: <TError = ErrorType<unknown>, TContext = unknown>(options?: {
    mutation?: UseMutationOptions<Awaited<ReturnType<typeof evaluateEligibility>>, TError, EvaluateEligibilityMutationVariables, TContext>;
    request?: SecondParameter<typeof customFetch>;
}) => UseMutationOptions<Awaited<ReturnType<typeof evaluateEligibility>>, TError, EvaluateEligibilityMutationVariables, TContext>;
export type EvaluateEligibilityMutationResult = NonNullable<Awaited<ReturnType<typeof evaluateEligibility>>>;
export type EvaluateEligibilityMutationBody = BodyType<EligibilityInput>;
export type EvaluateEligibilityMutationError = ErrorType<unknown>;
export type EvaluateEligibilityMutationVariables = {
    data: BodyType<EligibilityInput>;
};
export declare const useEvaluateEligibility: <TError = ErrorType<unknown>, TContext = unknown>(options?: {
    mutation?: UseMutationOptions<Awaited<ReturnType<typeof evaluateEligibility>>, TError, EvaluateEligibilityMutationVariables, TContext>;
    request?: SecondParameter<typeof customFetch>;
}) => UseMutationResult<Awaited<ReturnType<typeof evaluateEligibility>>, TError, EvaluateEligibilityMutationVariables, TContext>;
export declare const getGetDecisionModelUrl: () => string;
export declare const getDecisionModel: (options?: Parameters<typeof customFetch>[1]) => Promise<GetDecisionModel200>;
export declare const getGetDecisionModelQueryKey: () => readonly ["/api/rules/decision-model"];
export declare const getGetDecisionModelQueryOptions: <TData = Awaited<ReturnType<typeof getDecisionModel>>, TError = ErrorType<unknown>>(options?: {
    query?: UseQueryOptions<Awaited<ReturnType<typeof getDecisionModel>>, TError, TData>;
    request?: SecondParameter<typeof customFetch>;
}) => UseQueryOptions<Awaited<ReturnType<typeof getDecisionModel>>, TError, TData> & {
    queryKey: QueryKey;
};
export type GetDecisionModelQueryResult = NonNullable<Awaited<ReturnType<typeof getDecisionModel>>>;
export type GetDecisionModelQueryError = ErrorType<unknown>;
export declare function useGetDecisionModel<TData = Awaited<ReturnType<typeof getDecisionModel>>, TError = ErrorType<unknown>>(options?: {
    query?: UseQueryOptions<Awaited<ReturnType<typeof getDecisionModel>>, TError, TData>;
    request?: SecondParameter<typeof customFetch>;
}): UseQueryResult<TData, TError> & {
    queryKey: QueryKey;
};
export {};
//# sourceMappingURL=api.d.ts.map