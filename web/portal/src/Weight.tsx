import React, {useState} from "react";
import TimeKeeper from "react-timekeeper";
import InputNumber from "rc-input-number";
import {Button} from "react-bootstrap";

type WeightProps = {
    day: string
}

const Weight = (props: WeightProps) => {
    const day = props.day
    const [weight, setWeight] = useState(100)

    const saveWeight = () => {
        fetch("http://localhost:8080", {
            method: "put",
            mode: 'cors',
            headers: {"Content-type": "application/json"},
            body: JSON.stringify({
                "userId": "1",
                "typeId": "2",
                "date": day,
                "weight": weight
            })
        }).then(r => {})
    }

    return (
        <>
            <InputNumber onChange={(weight) => setWeight(Number(weight))} step={0.1} defaultValue={weight}/>
            <Button onClick={saveWeight}>Save weight for today</Button>
        </>
    )
}

export default Weight