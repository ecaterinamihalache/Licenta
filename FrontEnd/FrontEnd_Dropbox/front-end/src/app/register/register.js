import React, { Component } from 'react';
import { FormErrors } from '../formErrors/formErrors.js';
import { FaEnvelope } from "react-icons/fa";
import { FaLock } from "react-icons/fa";
import { FaUser } from "react-icons/fa";
import axios from "axios";
import './register.css';

export default class Register extends Component{
    constructor(props){
      super(props);
      this.state = {
        firstname : '',
        lastname : '',
        email : '',
        password : '',
        passwordConfirm : '',
        formErrors: {firstname : '', lastname : '', email: '', password: ''},
        firstnameValid: false,
        lastnameValid: false,
        emailValid: false,
        passwordValid: false,
        passwordConfirmValid: false,
        formValid: false
      }
    }

    account = {
      id : 0,
      email : '',
      password : '',
      role : '',
      firstname : '',
      lastname : ''
    }
      
    async createAccount(info) {
      const actualData = await fetch('http://127.0.0.1:8082/accounts/', { //http://127.0.0.1:8080/uac/uac/accounts/
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(info)
       }).then(response => response.json())
      .catch(function(error) {
        //console.log('Fetch Error:', error);
        window.location.href = "/register";
      });
      //console.log(actualData);
      return actualData;
  }

  validateField(fieldName, value) {
    let fieldValidationErrors = this.state.formErrors;
    let firstnameValid = this.state.firstnameValid;
    let lastnameValid = this.state.lastnameValid;
    let emailValid = this.state.emailValid;
    let passwordValid = this.state.passwordValid;
    let passwordConfirmValid = this.state.passwordConfirmValid;
  
    switch(fieldName) {
      case 'firstname':
        firstnameValid = value.length >= 4;
        fieldValidationErrors.firstname = firstnameValid ? '' : ' is too short';
        break;
      case 'lastname':
        lastnameValid = value.length >= 4;
        fieldValidationErrors.lastname = lastnameValid ? '' : ' is too short';
        break;
      case 'email':
        emailValid = value.match(/^([\w.%+-]+)@([\w-]+\.)+([\w]{2,})$/i);
        fieldValidationErrors.email = emailValid ? '' : ' is invalid';
        break;
      case 'password':
        passwordValid = value.length >= 6;
        fieldValidationErrors.password = passwordValid ? '': ' is too short';
        break;
      case 'passwordConfirm':
        passwordConfirmValid = value.match(this.state.password);
        fieldValidationErrors.passwordConfirm = passwordConfirmValid ? '': ' it is not the same as the previous one ';
        break;
      default:
        break;
    }
    this.setState({formErrors: fieldValidationErrors, firstnameValid: firstnameValid, lastnameValid: lastnameValid, emailValid: emailValid, passwordValid: passwordValid, passwordConfirmValid:passwordConfirmValid}, this.validateForm);
  }
  
  validateForm() {
    this.setState({formValid: this.state.firstnameValid && this.state.lastnameValid && this.state.emailValid && this.state.passwordValid && this.state.passwordConfirmValid});
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
    let firstname = this.state.firstname;
    let lastname = this.state.lastname;

    this.account = await this.createAccount({
      email,
      password,
      role : "USER",
      firstname,
      lastname
    });

    console.log(this.account);
    console.log(this.account.id);
    console.log(this.account.email);
    let user = {
      id : this.account.id,
      email : this.account.email,
      files : []
    }
    console.log(user);

    axios.post("http://127.0.0.1:8082/users", user, //http://localhost:8081/manager/users
    {
      headers: 
      {
        "Content-Type": "application/json"
      }
    });
    window.location.href = "/login";
  }

  render(){
    return <div className="login-box">
      <br/>
      <h1>Register</h1>
      <form onSubmit={this.handleSubmit}>
        <div className="formSpace">
          <FaUser style={{fontSize: '27px'}}  />
          <input type="text" name="firstname" placeholder="Firstname" className="inputClass" onChange = {(e) => this.handleUserInput(e)}/>
        </div>

        <div className="formSpace">
          <FaUser style={{fontSize: '27px'}}  />
          <input type="text" name="lastname" placeholder="Lastname" className="inputClass" onChange = {(e) => this.handleUserInput(e)}/>
        </div>

        <div className="formSpace">
          <FaEnvelope style={{fontSize: '27px'}} />
          <input type="text" name="email" placeholder="Email" className="inputClass" onChange= {(e) => this.handleUserInput(e)}/>
        </div>

        <div className="formSpace">
          <FaLock style={{fontSize: '27px'}} />
          <input type="password" name="password" placeholder="Password" className="inputClass" onChange= {(e) => this.handleUserInput(e)}/>
        </div>

        <div className="formSpace">
          <FaLock style={{fontSize: '27px'}} />
          <input type="password" name="passwordConfirm" placeholder="Confirm password" className="inputClass" onChange= {(e) => this.handleUserInput(e)}/>
        </div>

        <div>
          <FormErrors formErrors={this.state.formErrors} />
        </div>

        <button className="button" type="submit" disabled={!this.state.formValid}>Register</button>
      </form>
    </div>
  }
}
