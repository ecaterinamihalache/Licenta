import React from 'react';
import './index.css';
import 'bootstrap/dist/css/bootstrap.css';
import App from './app/app';

import {createRoot} from 'react-dom/client';

const rootElement = document.getElementById("root");
const root = createRoot(rootElement);

root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

