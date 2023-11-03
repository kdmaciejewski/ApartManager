import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Apartment} from "../../../../generated";

@Injectable({
  providedIn: 'root'
})
export class ApartmentService {

  constructor(private http: HttpClient) { }

  getApartments(): Observable<Apartment[]> {
    return this.http.get<Apartment[]>('/api/11/apartment');
    //TODO change path
  }

  addApartment(apartment: Apartment): Observable<boolean> {
    return this.http.post<boolean>('/api/11/apartment', apartment);
  }
}