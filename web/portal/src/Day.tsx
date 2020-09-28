import React from "react";
import Moment from "react-moment"
import moment from "moment"
import Weight from "./Weight";

export const Day = () => {
    const day = new Date()

    return (
        <div>
            <Moment format={"YYYY/MM/DD"}>{day}</Moment>
            <Weight day={moment(day).format("YYYY-MM-DD")}/>
        </div>
    )
}