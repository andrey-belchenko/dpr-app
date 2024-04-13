import React from 'react';
import ReactDOM from 'react-dom';
import 'react-perfect-scrollbar/dist/css/styles.css';
import './index.css';

import App from './MyApp';
import reportWebVitals from './reportWebVitals';


document.title = "АСТУ: Инструмент подготовки данных"

ReactDOM.render(
    <React.StrictMode>
     <App/>
    </React.StrictMode>,
  document.getElementById('root')
);
reportWebVitals();
