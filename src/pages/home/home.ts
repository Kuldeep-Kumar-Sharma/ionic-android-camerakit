import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';



@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
 image="./assets/imgs/image.svg";
  constructor(public navCtrl: NavController) {

  }
  start() {
    window['plugins'].start.camera(
      "Sample Application", (result) => {
        console.log(result);
        let imagesources = result.split(",")
        console.log(imagesources);
        this.image = imagesources[0];
        console.log(this.image);
      },
      (err) => {
        console.log(err);
      },

    );
  }
}

