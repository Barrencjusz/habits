import React, {useEffect, useState} from "react";
import Table from "react-bootstrap/Table"
import Moment from "react-moment"

type HabbitHistory = {
    type: string,
    dates: Array<Habit>
}

type Habit = {
    date: string,
    success?: boolean
    custom?: any
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
                    <Table key={habitHistory.type} striped bordered hover>
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>{habitHistory.type}</th>
                        </tr>
                        </thead>
                        <tbody>
                        {habitHistory.dates.map(date => (
                            <tr key={date.date}>
                                <td><Moment format="YYYY/MM/DD">{date.date}</Moment></td>
                                <td>{
                                    date.success !== undefined ? (date.success + "") : date.custom?.weight
                                }</td>
                            </tr>
                        ))}
                        </tbody>
                    </Table>
                ))
            }
        </div>
    );
}

export default Habits;