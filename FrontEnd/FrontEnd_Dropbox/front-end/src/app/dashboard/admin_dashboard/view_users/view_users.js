import React, { Component } from 'react';
import './view_users.css';
import { Client } from '@stomp/stompjs';

const SOCKET_URL = 'ws://127.0.0.1:8081/users';

export default class ViewUsers extends Component {
    constructor(props){
        super(props)
        this.state = {
            data: [
                {
                    id : '',
                    files : [],
                }
            ]
        }
    }

    async componentDidMount(){
        this.client = new Client();

        this.client.configure({
        brokerURL: SOCKET_URL,
        onConnect: () => {
            console.log('onConnect');
            this.client.subscribe('/topic/users', message => {
                console.log(eval(message.body));
                this.setState({data : eval(message.body)});
            });
        },
        debug: (str) => {
            console.log(new Date(), str);
        }
        });
        this.client.activate();
    }
    
    render() {
        const users = this.state.data;
        return (
            <div>
                <table className="table table-striped">
                    <thead>
                        <tr>
                            <th>UserId</th>
                            <th>Files</th>
                        </tr>
                    </thead>
                    <tbody>
                    {
                        users.map((item) => (
                            <tr key={item.id}>
                                <td>{item.id}</td>
                                <td>
                                    {item.files.map((file, index) => 
                                        <ul key={index}>{file}</ul>
                                    )}
                                </td>
                            </tr>
                        ))
                    }
                    </tbody>
                </table>
            </div>
        )
    }
}