import './polyfills';
import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import TestDiagram from './sandbox/TestDiagram';


// const forceSlashAfterHash = () => {

//   let _hash = window.location.hash;
  
//   if (_hash[1] && _hash[1] != '/') {

//       window.location.href = window.location.origin + window.location.pathname + window.location.search + "#/" + _hash.slice(1);

//   }
// }

// forceSlashAfterHash();


// window.addEventListener('hashchange', forceSlashAfterHash);

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <React.StrictMode>
    <App />
    {/* <TestDiagram/> */}
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
