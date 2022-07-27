import React, { Component } from 'react';
import { setCookie} from '../../util/functions_cookies/cookiesFunctions.js';
import { FormErrors } from '../formErrors/formErrors.js';
import { FaEnvelope } from "react-icons/fa";
import { FaLock } from "react-icons/fa";
import './login.css';

export default class Login extends Component
{
  constructor(props){
    super(props);

    this.state = {
      email: '',
      password: '',
      formErrors: {email: '', password: ''},
      emailValid: false,
      passwordValid: false,
      formValid: false
    };
  }

  tokenAndClaims = {
    token : '',
    accountId : 0,
    role : ''
  };

  async loginUser(credentials) {
    const actualData = await fetch('http://127.0.0.1:8082/authenticate/', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(credentials)
    }).then(response =>  response.json())
    .catch(function(error) {
      console.log('Fetch Error:', error);
    });
    console.log(actualData);
    return actualData;
  }

  validateField(fieldName, value) {
    let fieldValidationErrors = this.state.formErrors;
    let emailValid = this.state.emailValid;
    let passwordValid = this.state.passwordValid;
  
    switch(fieldName) {
      case 'email':
        emailValid = value.match(/^([\w.%+-]+)@([\w-]+\.)+([\w]{2,})$/i);
        fieldValidationErrors.email = emailValid ? '' : ' is invalid';
        break;
      case 'password':
        passwordValid = value.length >= 6;
        fieldValidationErrors.password = passwordValid ? '': ' is too short';
        break;
      default:
        break;
    }
    this.setState({formErrors: fieldValidationErrors, emailValid: emailValid, passwordValid: passwordValid}, this.validateForm);
  }
  
  validateForm() {
    this.setState({formValid: this.state.emailValid && this.state.passwordValid});
  }

  handleUserInput (e) {
    const name = e.target.name;
    const value = e.target.value;
    this.setState({[name]: value},
      () => { this.validateField(name, value) });
  }

  handleSubmit = async e => {
    e.preventDefault();
    let email = this.state.email;
    let password = this.state.password;
    localStorage.clear();
    localStorage.setItem('email', email);
    this.tokenAndClaims = await this.loginUser({
      email,
      password
    });

    console.log(this.tokenAndClaims);

    // localStorage.setItem('token', this.tokenAndClaims.token);
    localStorage.setItem('accountId', this.tokenAndClaims.accountId);
    localStorage.setItem('role', this.tokenAndClaims.role);

    setCookie('tokenCookie', this.tokenAndClaims.token, 20);
  
    if(this.tokenAndClaims.role === 'USER')
    {
      window.location.href = "/user_home";
    }
    else if(this.tokenAndClaims.role === 'ADMIN')
    {
      window.location.href = "/view_filesMap";
    }
    else
    {
      window.location.href = "/";
    }
  }

  render(){
    return (
      <div className="login-box">
        <br/><br/><br/>
        <h1>Login</h1>
        <form onSubmit={this.handleSubmit}>
          <div className="column justify-content-center" >
            <div className="formSpace">
              <FaEnvelope style={{fontSize: '27px'}} />
              <input name="email" type="text" placeholder="Email" className="inputClass" onChange = {(e) => this.handleUserInput(e)}/>
            </div>

            <div className="formSpace">
              <FaLock style={{fontSize: '27px'}} />
              <input name="password" type="password" placeholder="Password" className="inputClass" onChange = {(e) => this.handleUserInput(e)}/>
            </div>

            <div className="errorsMessg">
              <FormErrors formErrors={this.state.formErrors} />
            </div>

            <div>
              <button className="button" type="submit" disabled={!this.state.formValid}>Login</button>
            </div>
          </div>
        </form>
        <br/>
        <a href="/register">Register</a>
      </div>
    );
  }
}
