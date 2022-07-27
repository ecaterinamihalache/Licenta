import React, { Component} from 'react';
import { BrowserRouter, Routes , Route, Navigate } from 'react-router-dom';
import Login from './login/login';
import Register from './register/register';
import UserHome from './dashboard/user_dashboard/user_home/user_home';
import ViewUsers from './dashboard/admin_dashboard/view_users/view_users';
import ViewFilesMap from './dashboard/admin_dashboard/view_filesMap/view_filesMap';
import { FaBoxOpen } from "react-icons/fa";
import './app.css';
import { getCookie } from '../util/functions_cookies/cookiesFunctions';

export default class App extends Component {
  constructor(props){
    super(props);
    this.state = { isLogin : false,
    email: '',
    role: '' }; 
  }

  onLogout = () => {
    localStorage.clear();
    window.location.href = "/";
  }

  async componentDidMount(){
    if(getCookie('tokenCookie') != null && localStorage.getItem('accountId') != null && localStorage.getItem('role') != null && localStorage.getItem('role') === 'ADMIN'){
      this.setState({isLogin : true, email: localStorage.getItem('email'), role: 'ADMIN'})
    }else if(getCookie('tokenCookie') != null && localStorage.getItem('accountId') != null && localStorage.getItem('role') != null && localStorage.getItem('role') === 'USER'){
      this.setState({isLogin : true, email: localStorage.getItem('email'), role: 'USER'})
    }
    else{
      this.setState({isLogin : false, email: '', role: ''})
    }
  }

  render() {
    return (
        <div className="App">
          <BrowserRouter>
            <div>
              <div className="header" style={{height: '50px'}}>
                {
                  this.state.isLogin ? 
                  (
                    <div>
                      {
                        this.state.role === 'ADMIN' ?
                        (
                          <div>
                            <div className="iconClass">
                              <FaBoxOpen style={{fontSize: '30px'}} />
                            </div>
                            <a href="/view_users">Users</a>
                            <a>Welcome, {this.state.email}!</a>
                            <a href="/" onClick={this.onLogout}>LogOut</a>
                          </div>
                        )
                        :
                        (
                          <div>
                            <div className="iconClass">
                              <FaBoxOpen style={{fontSize: '30px'}} />
                            </div>
                            <a>Welcome, {this.state.email}!</a>
                            <a href="/" onClick={this.onLogout}>LogOut</a>
                          </div>
                        )
                      }
                    </div>
                  )
                  :
                  (
                    <div className="iconClass">
                      <FaBoxOpen style={{fontSize: '30px'}} />
                    </div>
                  )
                }
              </div>
              
              <div className="content">
                <Routes>
                  <Route path="/" element={<Login/>}/>
                  <Route path="/register" element={<Register/>}/>
                  <Route path="/user_home" element={<UserHome/>}/>
                  <Route path="/view_users" element={<ViewUsers/>}/>
                  <Route path="/view_filesMap" element={<ViewFilesMap/>}/>
                  <Route path="*" element={<Navigate replace to = "/" />}/>
                </Routes>
              </div>
            </div>
          </BrowserRouter>
        </div>
    );
  }
}

