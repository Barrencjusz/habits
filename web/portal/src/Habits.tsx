import React, {useState, useEffect} from "react";
import ReactDOM from "react-dom";

// import "./styles.css";

type HabbitHistory = {
    type: string,
    dates: Array<Habit>
}

type Habit = {
    date: string,
    success: boolean
}

function Habits() {
    const [habbitHistories, setHabbitHistories] = useState<Array<HabbitHistory>>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetch(
            `http://localhost:8080/history`,
            {
                method: "GET"
            }
        )
            .then(res => res.json())
            .then(response => {
                setHabbitHistories(response);
                setIsLoading(false);
            })
            .catch(error => console.log(error));
    }, []);

    return (
        <div>
            <h1>My habits</h1>
            {isLoading && <p>Wait I'm Loading Habits for you</p>}
            {
                habbitHistories && habbitHistories.map(habitHistory => (
                    <table>
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>{habitHistory.type}</th>
                        </tr>
                        </thead>
                        <tbody>
                        {habitHistory.dates.map(date => (
                            <tr>
                                <td>{date.date}</td>
                                <td>{date.success + ""}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                ))
            }
        </div>
    );
}

export default Habits;