import {Component, OnInit} from '@angular/core';
import {RdsInstancesService} from "../../../services/rds-instances.service";
import {BaseFormComponent} from "../../base-form.component";
import {ToastService} from "../../../utils/toast.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Subscription} from "rxjs";
import {RdsInstanceResponse} from "../../../services/types";
import {ContextService} from "../../navigation/context.service";

@Component({
  selector: 'app-rds-instances-details',
  templateUrl: './rds-instances-details.component.html',
})
export class RdsInstancesDetailsComponent extends BaseFormComponent implements OnInit {

  private subscription: Subscription;

  rdsInstance: RdsInstanceResponse;

  constructor(private contextService: ContextService, private router: Router, private route: ActivatedRoute, private rdsInstancesService: RdsInstancesService, private toastsService: ToastService) {
    super(toastsService);
  }

  ngOnInit(): void {
    this.subscription = this.route.params.subscribe(
      (next) => {
        this.rdsInstancesService.get(next['rdsInstanceId']).subscribe(
          (next) => {
            this.rdsInstance = next.rdsInstance
            this.contextService.nextRdsInstance(next.rdsInstance)
          },
          (error) => {
            this.toastsService.handleErrorResponse(error)
          }
        )
      });
  }

  delete(id: string) {
    this.rdsInstancesService.delete(id).subscribe((next) => {
        this.router.navigate(['providers', this.contextService.currentProviderId.value])
      },
      (error) => {
        this.toastsService.handleErrorResponse(error)
      }
    )
  }
}
