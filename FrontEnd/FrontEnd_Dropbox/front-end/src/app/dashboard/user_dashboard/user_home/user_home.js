import React, { Component } from 'react';
import axios from "axios";
import { getCookie } from '../../../../util/functions_cookies/cookiesFunctions';
import fileDownload from 'js-file-download';

export default class UserHome extends Component {
    state = { 
        selectedFile: null,
        usersID: [],
        data: [], 
        item: '',
        checked: false
    }; 
       
    onFileChange = event => { 
        this.setState({ selectedFile: event.target.files[0] }); 
    }; 
     
    onFileUpload = () => { 
        const formData = new FormData(); 
        const filesUpload = [];
  
        formData.append("file", this.state.selectedFile); 
        formData.append("idUser", localStorage.getItem("accountId"));
       
        console.log(this.state.selectedFile); 
        filesUpload.push(this.state.selectedFile.name);
        
        let user = {
          id : localStorage.getItem('accountId'),
          email : localStorage.getItem('email'),
          files : filesUpload
        }
        let token = getCookie('tokenCookie');
  
        axios.post("http://127.0.0.1:8082/users", user, 
        {
          headers: 
          {
            "Content-Type": "application/json",
            'Authorization': `Bearer ${token}`
          }
        });
  
        axios.post("http://127.0.0.1:8082/writenodefree", formData,
        {
          headers: 
          {
            "Content-Type": "multipart/form-data",
            'Authorization': `Bearer ${token}`
          }
        }); 

        window.location.href = "/user_home";
    }; 

    async componentDidMount(){
        let userID = localStorage.getItem('accountId'); 
        let token = getCookie('tokenCookie'); 
        let url = 'http://127.0.0.1:8082/usersFiles/'+ userID; 

        const header = { 'Content-Type': 'application/json',
                         'Authorization': `Bearer ${token}` };
        const files = await fetch(url,
        {
            method: 'GET',
            headers: header
        });

        const response = await files.json();
        this.setState({data : response}); 
    }

    onFileDownload = () => { 
        let token = getCookie('tokenCookie');
        console.log("aici in functie!");
        console.log(this.state.item);

        let downloadFile = {
            id : localStorage.getItem('accountId'),
            fileName : this.state.item
        }

        console.log("se face request-ul!");
        axios.post("http://127.0.0.1:8082/downloadFile", downloadFile,
        {
          headers: 
          {
            "Content-Type": "application/json",
            'Authorization': `Bearer ${token}`,
            responseType: 'blob'
          }
        })
        .then((res) => 
        { 
            fileDownload(res.data, downloadFile.fileName)
        });
    }; 

    
    handleChange(e, item) {
        let isChecked = e.target.checked;
        if(isChecked){
            console.log(item);
            this.setState({checked : true});
        }else{
            this.setState({checked : false});
        }
        this.setState({item : item});
    }
  
    render(){
        return <div>
          <div className="row"> 
            <div className="col">
                <input type="file" className="form-control" onChange={this.onFileChange} />
            </div>
            
            <div className="col divClass">
                <button onClick={this.onFileUpload} className="btn btn-secondary" style={{margin: '0% 5% 0 0'}}> 
                    Upload file
                </button>
            
                <button onClick={this.onFileDownload} className="btn btn-secondary" style={{margin: '0% 5% 0 0'}}> 
                    Download file
                </button>  
            </div>
          </div> 
          <br /><br /><br />


            <table className="table table-striped" style={{width:'75%'}}>
                <thead>
                    <tr>
                        <th style={{width:'10%'}}></th>
                        <th>Files</th>
                    </tr>
                </thead>
                
                <tbody>
                {
                    this.state.data.map((item) => (
                        <tr key={item}>
                            <td>
                                <input type="checkbox" disabled={this.state.checked && this.state.item!==item} onChange={e => this.handleChange(e, item)} />
                            </td>
                            <td>{item}</td>
                        </tr>
                    ))
                }
                </tbody>
            </table>
        </div>
    }
}
