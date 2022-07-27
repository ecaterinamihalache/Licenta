import React, { Component } from 'react';
import './view_filesMap.css';
import { Client } from '@stomp/stompjs';

const SOCKET_URL = 'ws://127.0.0.1:8081/hashMapFiles';

export default class ViewFilesMap extends Component {
      
    constructor(props){
        super(props)
        //am nevoie sa salvez datele vechi
        this.state = { //dateleActuale
            data: [
                {
                    nodeName: '',
                    files : []
                }
            ],

            dataLatest: [
                {
                    nodeName: '',
                    files : []
                }
            ],

            addedFiles: [],
            deletedFiles: []
        }
    }

    machObject(){
        console.log(this.state.dataLatest);
        if(this.state.data.length === this.state.dataLatest.length){
            console.log("vectorii sunt egali, verific daca si listele de fisiere sunt egale");
            for(let i=0;i<this.state.data.length;i++){
                if(this.state.data[i].files.length === this.state.dataLatest[i].files.length){
                    console.log("Vectorii sunt egali si listele de fisierele sunt egale! Nu este nicio modificare!");
                }else{
                    console.log("vectorii sunt egali dar listele de fisierele nu sunt egale!");
                    //verific care dintre liste este mai mare (concomitent pt fiecare obiect)
                    if(this.state.data[i].files.length > this.state.dataLatest[i].files.length){
                        console.log("Noua lista de fisiere este mai mare! Fisiere noi!");
                        //verific ce fisiere sunt noi si le adaug in addedFiles
                    }else{
                        console.log("Noua lista de fisiere este mai mica! S-au sters fisiere!");
                        //verific ce fisiere sunt in lista anterioara si nu sunt in lista actuala si le adaug in deletedList
                    }
                }
            }
            this.setState({dataLatest : this.state.data});
        }else{
        console.log("vectorii nu sunt egali!");
        //verific care vector este mai mare
        if(this.state.data.length > this.state.dataLatest.length){
            console.log("Vectorul primit este mai mare! S-a deschis un nod!");
            for(let i=0;i<this.state.data.length;i++){
                if(this.state.dataLatest[i] != null){
                    if(this.state.data[i].files.length === this.state.dataLatest[i].files.length){ //verific daca obiectele sunt la fel
                        console.log("Sunt la fel");
                    }
                }else{
                    for(let j=0;j<this.state.data[i].files.length;j++){
                        console.log(this.state.data[i].files[j]);
                        this.state.addedFiles.push(this.state.data[i].files[j]);
                    }
                    console.log("lista de fisiere adaugate");
                    console.log(this.state.addedFiles);
                    this.setState({addedFiles : this.state.addedFiles});
                }
            }
            //verific ce obiecte sunt in plus in vector si pun in lista de addedFiles fisierele din aceste obiecte
        }else{
            console.log("Vectorul primit este mai mic! S-a inchis un nod!");
            for(let i=0;i<this.state.dataLatest.length;i++){
                if(this.state.data[i] != null){
                    if(this.state.data[i].files.length === this.state.dataLatest[i].files.length){ //verific daca obiectele sunt la fel
                        console.log("Sunt la fel");
                    }
                }else{
                    for(let j=0;j<this.state.dataLatest[i].files.length;j++){
                        console.log(this.state.dataLatest[i].files[j]);
                        this.state.deletedFiles.push(this.state.dataLatest[i].files[j]);
                        }
                        console.log("lista de fisiere sterse");
                        console.log(this.state.deletedFiles);
                        this.setState({deletedFiles : this.state.deletedFiles});
                    }
                }
                //verific ce obiecte sunt in plus in vector vechi si pun in lista de deletedFiles fisierele din aceste obiecte
            }
            this.setState({dataLatest : this.state.data});
        }
    }

    async componentDidMount(){
        this.client = new Client();

        this.client.configure({
        brokerURL: SOCKET_URL,
        onConnect: () => {
            console.log('onConnect');
            this.client.subscribe('/topic/hashMapFiles', message => {
                this.setState({data : eval(message.body)});
                this.machObject();
            });  
        },
        debug: (str) => {
            console.log(new Date(), str);
        }
        });
        this.client.activate();
    }

    render() {
        const data = this.state.data;
        return (
            <div>
                <table className="table table-striped">
                    <thead>
                        <tr>
                            <th>NodeName</th>
                            <th>IdUser/File</th>
                        </tr>
                    </thead>

                    <tbody>
                    {
                        data.map((item) => (
                            <tr key={item.nodeName}>
                                <td>{item.nodeName}</td>
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