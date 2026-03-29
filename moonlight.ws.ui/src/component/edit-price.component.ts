import { Component, Input, model, OnChanges, OnInit, SimpleChanges, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { BehaviorSubject, combineLatest } from 'rxjs';
import { createNumberPropertyDefined, createStringPropertyDefined } from '../util/component.util';
import { Price } from '../rest/model/price';
import { isValidFiniteNumber } from '../util/number.util';

@Component({
    selector: 'mlws-edit-price',
    imports: [
        CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatRadioModule,
        ReactiveFormsModule, MatOptionModule, MatSelectModule
    ],
    templateUrl: './edit-price.component.html',
    styleUrls: ['./edit-price.component.scss'],
    encapsulation: ViewEncapsulation.None
})
@UntilDestroy()
export class EditPriceComponent implements OnInit, OnChanges {
    @Input()
    public quantity: number | undefined;

    /**
     * The price as it was entered by the user and is to be written to the server.
     */
    public price = model<Price|undefined>(undefined);

    // private readonly priceEffect = effect(() => {
    //     const price = this.price();
    //     if (price == undefined) {
    //         this.priceInput$.next(undefined);
    //     } else {
    //         this.priceInput$.next(price.priceTotalGross);
    //     }
    // });

    /**
     * The locally calculated price. This should not be written to the server, because the server should calculate itself directly from the
     * user input.
     */
    public priceCalculated = model<Price|undefined>(undefined);

    public readonly taxPercentages: number[] = [0, 7];

    protected quantityOriginal: number | undefined;
    protected priceInputOriginal: number | undefined;
    protected taxModeOriginal: TaxMode = TaxMode.NET; // overwritten in ngOnInit()!
    protected totalSingleModeOriginal: TotalSingleMode = TotalSingleMode.SINGLE; // overwritten in ngOnInit()!
    protected taxPercentOriginal = 0; // overwritten in ngOnInit()!
    protected pristine = true;

    protected readonly quantity$ = new BehaviorSubject<number|undefined>(undefined);

    protected get taxMode(): TaxMode {
        return this.taxMode$.getValue() as TaxMode;
    }
    protected set taxMode(v: TaxMode) {
        if (this.taxMode !== v) {
            this.taxMode$.next(v);
        }
    }
    protected readonly taxMode$: BehaviorSubject<string>;

    protected get totalSingleMode(): TotalSingleMode {
        return this.totalSingleMode$.getValue() as TotalSingleMode;
    }
    protected set totalSingleMode(v: TotalSingleMode) {
        if (this.totalSingleMode !== v) {
            this.totalSingleMode$.next(v);
        }
    }
    protected readonly totalSingleMode$: BehaviorSubject<string>;

    protected get priceInput(): number | undefined {
        return this.priceInput$.getValue();
    }
    protected set priceInput(value: number | undefined) {
        if (this.priceInput !== value) {
            this.priceInput$.next(value);
        }
    }
    protected readonly priceInput$ = new BehaviorSubject<number | undefined>(undefined);

    protected get taxPercent(): number {
        return this.taxPercent$.getValue();
    }
    protected set taxPercent(value: number) {
        if (this.taxPercent !== value) {
            this.taxPercent$.next(value);
        }
    }
    protected readonly taxPercent$: BehaviorSubject<number>;

    public constructor() {
        this.taxMode$ = createStringPropertyDefined(this, "EditPriceComponent.taxMode", TaxMode.GROSS);
        this.totalSingleMode$ = createStringPropertyDefined(this, "EditPriceComponent.totalSingleMode", TotalSingleMode.TOTAL);
        this.taxPercent$ = createNumberPropertyDefined(this, "EditPriceComponent.taxPercent", 0);
    }

    public ngOnInit() {
        this.quantityOriginal = this.quantity;
        this.priceInputOriginal = this.priceInput;
        this.taxModeOriginal = this.taxMode;
        this.totalSingleModeOriginal = this.totalSingleMode;
        this.taxPercentOriginal = this.taxPercent;
        this.pristine = true;
        this.initPriceCalculation();
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes["quantity"]) {
            this.quantity$.next(this.quantity);
        }
        if (changes["price"]) {
            const price = changes["price"].currentValue as Price;
            if (price == undefined) {
                this.priceInput = undefined;
            } else {
                this.taxPercent = price.taxPercent!;
                switch (this.totalSingleMode) {
                    case TotalSingleMode.SINGLE:
                        switch (this.taxMode) {
                            case TaxMode.GROSS:
                                this.priceInput = price.priceSingleGross;
                                break;
                            case TaxMode.NET:
                                this.priceInput = price.priceSingleNet;
                                break;
                            default:
                                throw new Error(`Invalid taxMode: ${this.taxMode}`);
                        }
                        break;
                    case TotalSingleMode.TOTAL:
                        switch (this.taxMode) {
                            case TaxMode.GROSS:
                                this.priceInput = price.priceTotalGross;
                                break;
                            case TaxMode.NET:
                                this.priceInput = price.priceTotalNet;
                                break;
                            default:
                                throw new Error(`Invalid taxMode: ${this.taxMode}`);
                        }
                        break;
                    default:
                        throw new Error(`Unknown totalSingleMode: ${this.totalSingleMode}`);
                }
            }
        }
    }

    private initPriceCalculation(): void {
        combineLatest([this.quantity$, this.taxMode$, this.totalSingleMode$, this.priceInput$, this.taxPercent$])
            .pipe(untilDestroyed(this))
            .subscribe(([quantity, taxMode, totalSingleMode, priceInput, taxPercent]) => {
                if (this.pristine && quantity === this.quantityOriginal && taxMode === this.taxModeOriginal && totalSingleMode === this.totalSingleModeOriginal && priceInput === this.priceInputOriginal && taxPercent === this.taxPercentOriginal) {
                    return;
                }
                this.pristine = false;
                if (quantity && taxMode && totalSingleMode && isValidFiniteNumber(priceInput) && isValidFiniteNumber(taxPercent)) {
                    this.priceCalculated.set(this.calculatePrice(quantity, taxMode as TaxMode, totalSingleMode as TotalSingleMode, priceInput!, taxPercent));
                    this.price.set(this.createPriceOutput(quantity, taxMode as TaxMode, totalSingleMode as TotalSingleMode, priceInput!, taxPercent));
                } else {
                    this.priceCalculated.set(undefined)
                    this.price.set(undefined);
                }
            });
    }

    private createPriceOutput(quantity: number, taxMode: TaxMode, totalSingleMode: TotalSingleMode, price: number, taxPercent: number): Price {
        switch (totalSingleMode) {
            case TotalSingleMode.SINGLE:
                switch (taxMode) {
                    case TaxMode.GROSS:
                        return {
                            quantity,
                            taxPercent,
                            priceSingleGross: price
                        };
                    case TaxMode.NET:
                        return {
                            quantity,
                            taxPercent,
                            priceSingleNet: price
                        };
                    default:
                        throw new Error(`Invalid taxMode: ${taxMode}`);
                }
            case TotalSingleMode.TOTAL:
                switch (taxMode) {
                    case TaxMode.GROSS:
                        return {
                            quantity,
                            taxPercent,
                            priceTotalGross: price
                        };
                    case TaxMode.NET:
                        return {
                            quantity,
                            taxPercent,
                            priceTotalNet: price
                        };
                    default:
                        throw new Error(`Invalid taxMode: ${taxMode}`);
                }
            default:
                throw new Error(`Unknown totalSingleMode: ${totalSingleMode}`);
        }
    }

    private calculatePrice(quantity: number, taxMode: TaxMode, totalSingleMode: TotalSingleMode, price: number, taxPercent: number): Price {
        switch (totalSingleMode) {
            case TotalSingleMode.SINGLE:
                switch (taxMode) {
                    case TaxMode.GROSS:
                        return {
                            quantity,
                            taxPercent,
                            priceSingleGross: price,
                            priceTotalGross: price * quantity,
                            priceSingleNet: this.calculateNetFromGross(price, taxPercent),
                            priceTotalNet: this.calculateNetFromGross(price * quantity, taxPercent)
                        };
                    case TaxMode.NET:
                        return {
                            quantity,
                            taxPercent,
                            priceSingleGross: this.calculateGrossFromNet(price, taxPercent),
                            priceTotalGross: this.calculateGrossFromNet(price * quantity, taxPercent),
                            priceSingleNet: price,
                            priceTotalNet: price * quantity
                        };
                    default:
                        throw new Error(`Invalid taxMode: ${taxMode}`);
                }
            case TotalSingleMode.TOTAL:
                switch (taxMode) {
                    case TaxMode.GROSS:
                        return {
                            quantity,
                            taxPercent,
                            priceSingleGross: price / quantity,
                            priceTotalGross: price,
                            priceSingleNet: this.calculateNetFromGross(price / quantity, taxPercent),
                            priceTotalNet: this.calculateNetFromGross(price, taxPercent)
                        };
                    case TaxMode.NET:
                        return {
                            quantity,
                            taxPercent,
                            priceSingleGross: this.calculateGrossFromNet(price / quantity, taxPercent),
                            priceTotalGross: this.calculateGrossFromNet(price, taxPercent),
                            priceSingleNet: price / quantity,
                            priceTotalNet: price
                        };
                    default:
                        throw new Error(`Invalid taxMode: ${taxMode}`);
                }
            default:
                throw new Error(`Unknown totalSingleMode: ${totalSingleMode}`);
        }
    }

    private calculateNetFromGross(grossPrice: number, taxPercent: number): number {
        return grossPrice / (1 + (taxPercent / 100));
    }

    private calculateGrossFromNet(netPrice: number, taxPercent: number): number {
        return netPrice * (1 + (taxPercent / 100));
    }

    protected readonly TotalSingleMode = TotalSingleMode;
    protected readonly TaxMode = TaxMode;
    protected readonly Object = Object;
}

export enum TaxMode {
    GROSS = 'GROSS',
    NET = 'NET'
}

export enum TotalSingleMode {
    TOTAL = 'TOTAL',
    SINGLE = 'SINGLE'
}