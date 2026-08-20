import { Alert, type AlertVariants } from "@heroui/react";
import React, { useRef, useState } from "react";

const ALERT_DISPLAY_DURATION: number = 3_000;
const MOVE_OUT_DURATION: number = 500;

type AlertQueueProps = {
    receiver: (add_alert: (alert: AlertInfo) => void) => void
};

export type AlertInfo = {
    status: AlertVariants["status"],
    title: string,
    description: string|React.JSX.Element,
    elements?: Array<React.JSX.Element>
};

type AlertData = {
    alert: AlertInfo,
    id: number,
    moving_out: boolean
}

const format_alert = ({alert, id, moving_out}: AlertData) => (
    <Alert
        key={id}
        status={alert.status}
        className={moving_out ? "moveOutOfView" : ""}
    >
        <Alert.Indicator />
        <Alert.Content>
            <Alert.Title>{alert.title}</Alert.Title>
            <Alert.Description>{alert.description}</Alert.Description>
            {alert.elements}
        </Alert.Content>
    </Alert>
);

const AlertQueue = ({receiver}: AlertQueueProps) => {
    const next_alert_id = useRef(0);
    const clean_and_move_down_timeout = useRef(-1);
    const [alerts, set_alerts] = useState<Array<AlertData>>([]);
    function clean_and_move_down() {
        clean_and_move_down_timeout.current = -1;
        set_alerts(prev => prev.filter(({moving_out}) => !moving_out));
    }

    receiver((alert) => {
        let id = next_alert_id.current;
        set_alerts(prev => [{alert, id, moving_out: false}, ...prev]);
        next_alert_id.current++;
        setTimeout(() => {
            set_alerts(prev => {
                let newAlerts = [...prev];
                newAlerts.find(({id: a_id}) => a_id === id)
                    .moving_out = true;
                return newAlerts;
            });
            
            if (clean_and_move_down_timeout.current !== -1) {
                clearTimeout(clean_and_move_down_timeout.current)
            }
            setTimeout(clean_and_move_down, MOVE_OUT_DURATION);
        }, ALERT_DISPLAY_DURATION);
    });
    return (
        <div className="absolute bottom-0 right-0 grid w-full max-w-xl gap-4 overflow-clip">
            {alerts.map(format_alert)}
        </div>
    );
};

export default AlertQueue;