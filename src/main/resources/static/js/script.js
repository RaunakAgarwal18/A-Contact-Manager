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

//First request to create order

const paymentStart = ()=>{
    let amount = $(".payment_field").val();
    console.log(amount);
    if(amount==''||amount==null){
        // alert("Fill an amount.");
        swal("Oops!", "Fill an amount!", "error");
        return;
    }else if(amount<=0){
        // alert("amount cannot be less than 1");
        swal("Oops!", "Amount cannot be less than 1", "error");
        return;
    }

    //using ajax to create order
    $.ajax(
        {
            url:'/user/create_order',
            data:JSON.stringify({amount:amount, info:'order_request'}),
            contentType:'application/json',
            type:'POST',
            dataType:'json',
            success:function(response){
                //invoked when success
                console.log(response)
                if(response.status=='created'){
                    //open payment form
                    let options = {
                        key:'rzp_test_AqaY2LH9kz0r0k',
                        amount:response.amount,
                        currency:'INR',
                        name:'Smart Contact Manager',
                        description:'test payment',
                        image:'https://www.google.com/imgres?imgurl=https%3A%2F%2Fimg.freepik.com%2Fpremium-photo%2Finr-indian-currency-rupee-symbol-3d_373783-43.jpg&tbnid=-Ei60uRgUrT1TM&vet=12ahUKEwjHx47vjYiCAxVWm2MGHWM_DqcQMygHegUIARCBAQ..i&imgrefurl=https%3A%2F%2Fwww.freepik.com%2Fpremium-photo%2Finr-indian-currency-rupee-symbol-3d_19520167.htm&docid=43lHWLQuGu4L4M&w=626&h=410&q=INR%20Image&ved=2ahUKEwjHx47vjYiCAxVWm2MGHWM_DqcQMygHegUIARCBAQ',
                        order_id:response.id,
                        handler:function(response){
                            console.log(response.razorpay_payment_id)
                            console.log(response.razorpay_order_id)
                            console.log(response.razorpay_signature)
                            console.log('payment Successful!')

                            updatePaymentOnServer(
                                response.razorpay_order_id,
                                response.razorpay_payment_id,
                                "paid"
                            )

                        },
                        prefill: { //We recommend using the prefill parameter to auto-fill customer's contact information especially their phone number
                            "name": "", //your customer's name
                            "email": "",
                            "contact": "" //Provide the customer's phone number for better conversion rates 
                        },
                        notes: {
                            "address": "Raunak_IITG"
                        },
                        "theme": {
                            "color": "#3399cc"
                        }
                    };

                    let rzp = new Razorpay(options);
                    rzp.open();
                    rzp.on('payment.failed', function (response){
                        console.log(response.error.code);
                        console.log(response.error.description);
                        console.log(response.error.source);
                        console.log(response.error.step);
                        console.log(response.error.reason);
                        console.log(response.error.metadata.order_id);
                        console.log(response.error.metadata.payment_id);
                        swal("Oops!", "Payment failed!", "error");
                    });
                }
            },
            error:function(error){
                //invoked when error
                console.log(error)
                alert("something went wrong")
            }
        }
    )
}

function updatePaymentOnServer(order_id,payment_id, state)
{
    $.ajax({
        url:'/user/update_order',
            data:JSON.stringify({order_id:order_id,payment_id:payment_id,status:state}),
            contentType:'application/json',
            type:'POST',
            dataType:'json',
            success:function(response){
                swal("Good job!", "Payment Successfull!", "success");
            },
            error:function(error){
                swal("Good job!", "Your Payment Successfull but we did not catch up. We will contact you soon.", "error");
            }
    });
}