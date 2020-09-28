import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import Habits from './Habits';
import * as serviceWorker from './serviceWorker';
import TimeKeeper from 'react-timekeeper';
import 'rc-input-number/assets/index.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import {Day} from "./Day";
import {WeightChart} from "./WeightChart";

ReactDOM.render(
    <React.StrictMode>
        <Habits/>
        <Day/>
        <TimeKeeper/>
        <WeightChart/>
    </React.StrictMode>,
    document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
