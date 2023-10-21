console.log("This is the script file");

const toggleSideBar = ()=>{
    if($(".sidebar").is(":visible")){
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    }else{
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }
};

const search = ()=>{
    let s = document.getElementById("search-input");
    let query = s.value;
    console.log(query);
    if(query!=""){
        let url = `http://localhost:8080/search/${query}`
        fetch(url).then((response) =>{
            return response.json();
        }).then((data) => {
            console.log(data)
            let text = `<div class='list-group'>`
            if(data.length==0){
                text+=`<span style="color:gray">No such contact found!!</span>`
            }else{
                count = 1;
                while(count<=data.length){
                    contact = data[count-1]
                        text+=`<a href = '/user/contact/0/${contact.cid}' class = 'list-group-item list-group-item-action'> ${contact.name} ( ${contact.secondName} ) </a>`
                    count = count+1;
                    if(count>=5){
                        break;
                    }
                };
            }
            text+=`</div>`
            $(".search-result").html(text);
            $(".search-result").show();
        })
    }else{
        $(".search-result").hide();
    }
}