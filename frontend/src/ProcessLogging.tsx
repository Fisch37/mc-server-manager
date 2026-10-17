import { useRef, useState } from "react";
import type { CreationSocket } from "./api/server";

type ProcessLoggingArgs = {
    socket: CreationSocket,
    on_finish: () => any,
    on_failure: () => any
};

const ProcessLogging = ({socket, on_finish, on_failure}: ProcessLoggingArgs) => {
    const [lines, set_lines] = useState([]);
    const end = useRef(null);

    socket.addOnMessage(message => {
        if ("backlog" in message) {
            set_lines(prev => [...prev, ...message.backlog]);
        } else if ("line" in message) {
            set_lines(prev => [...prev, message.line]);
        }
        end.current?.scrollIntoView(false)
    });
    socket.addOnClose((e) => {
        if (e.wasClean)
            on_finish();
        else
            on_failure();
    });

    return (
        <div
            className="bg-gray-800 w-full rounded-t-2x1 font-mono"
        >
            {lines.map(line => (
                <p>{line}</p>
            ))}
            <div ref={end} />
        </div>
    )
};

export default ProcessLogging;