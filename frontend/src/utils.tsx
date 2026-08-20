import type { AlertInfo } from "./AlertQueue";
import type { ApiError } from "./api/shared";
import { queue_alert } from "./App";

export function alertApiError(
    e: any,
    handlers: {[response_code: number]: () => AlertInfo},
    other: (e: ApiError) => AlertInfo,
    unknown: () => AlertInfo
) {
    if ("type" in e && e.type === "api_error") {
        let error = e as ApiError;
        let handler = handlers[error.response_code];
        if (handler === undefined) {
            queue_alert(other(error));
        } else {
            queue_alert(handler());
        }
    } else {
        queue_alert(unknown());
        console.error(e);
    }
}
